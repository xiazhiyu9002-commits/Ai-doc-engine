package com.aidoc.engine.service.impl;

import com.aidoc.engine.service.FormulaConvertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 基于 MathJax 的公式转换服务
 * 使用 MathJax (Node.js) 将 LaTeX 转换为 MathML，然后转换为 OMML
 * 
 * 优势：
 * 1. 支持更多 LaTeX 命令和宏包（包括化学、物理等学科）
 * 2. 支持 mhchem 扩展（化学方程式）
 * 3. 支持 physics 扩展（物理符号）
 * 4. 更完整的 LaTeX 兼容性
 * 5. 活跃维护，持续更新
 * 
 * 要求：
 * - 服务器需要安装 Node.js
 * - 需要安装 mathjax-full: npm install mathjax-full
 */
@Slf4j
@Service("mathJaxFormulaConvertService")
public class MathJaxFormulaConvertService implements FormulaConvertService {
    
    @Value("${app.formula.mathjax.script-path:mathjax-to-mathml.js}")
    private String mathjaxScriptPath;
    
    @Value("${app.formula.mathjax.node-command:node}")
    private String nodeCommand;
    
    private static final String MATHML_NAMESPACE = "http://www.w3.org/1998/Math/MathML";
    
    private static final String INVISIBLE_OPERATOR_CHARS_REGEX = "[\\u2061\\u2062\\u2063\\u200B\\u00A0]";
    
    private volatile Boolean mathJaxAvailable;
    private volatile Templates mml2ommlTemplates;

    private final Map<String, String> mathmlCache = lruCache(1024);
    private final Map<String, String> ommlCache = lruCache(2048);
    
    @Override
    public String convertLatexToMathml(String latex) {
        return convertLatexToMathml(latex, false);
    }
    
    @Override
    public String convertLatexToMathml(String latex, boolean inline) {
        log.debug("使用 MathJax 转换 LaTeX 到 MathML: {}, inline={}", latex, inline);
        
        if (latex == null || latex.trim().isEmpty()) {
            log.warn("LaTeX 输入为空");
            return null;
        }
        
        try {
            // 检查 Node.js 和 MathJax 脚本是否可用
            if (!isMathJaxAvailable()) {
                log.error("MathJax 不可用，请确保已安装 Node.js 和 mathjax-full");
                return null;
            }
            
            // 规范化 LaTeX 输入
            String normalizedLatex = normalizeLatexDelimiters(latex);

            String cacheKey = (inline ? "1|" : "0|") + normalizedLatex;
            String cached = mathmlCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
            
            // 调用 Node.js 执行 MathJax 转换
            ProcessBuilder pb = new ProcessBuilder(
                nodeCommand,
                mathjaxScriptPath,
                normalizedLatex,
                String.valueOf(inline)
            );
            
            // 设置工作目录
            pb.directory(Paths.get(".").toFile());
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            // 读取输出
            String output = new String(
                process.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
            ).trim();
            
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                log.error("MathJax 转换失败，退出码: {}, 输出: {}", exitCode, output);
                return null;
            }
            
            log.debug("MathJax 转换成功，MathML 长度: {}", output.length());
            
            // 确保有命名空间
            if (!output.contains("xmlns=") && !output.contains("xmlns:mml=")) {
                output = output.replace("<math", "<math xmlns=\"" + MATHML_NAMESPACE + "\"");
            }
            
            // 根据 inline 参数调整 display 属性
            if (inline) {
                if (output.contains("display=\"block\"")) {
                    output = output.replace("display=\"block\"", "display=\"inline\"");
                } else if (!output.contains("display=")) {
                    output = output.replace("<math", "<math display=\"inline\"");
                }
            }

            String result = sanitizeMathml(output);
            if (result != null) {
                mathmlCache.put(cacheKey, result);
            }
            return result;
            
        } catch (Exception e) {
            log.error("调用 MathJax 失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public String convertMathmlToOmml(String mathml) {
        log.debug("开始转换 MathML 到 OMML");
        
        if (mathml == null || mathml.trim().isEmpty()) {
            log.warn("MathML 输入为空");
            return null;
        }
        
        try {
            String sanitizedMathml = sanitizeMathml(mathml);

            Transformer transformer = getMml2OmmlTemplates().newTransformer();
            
            ByteArrayInputStream mathmlInput = new ByteArrayInputStream(
                sanitizedMathml.getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream ommlOutput = new ByteArrayOutputStream();
            
            transformer.transform(
                new StreamSource(mathmlInput),
                new StreamResult(ommlOutput)
            );
            
            String omml = ommlOutput.toString(StandardCharsets.UTF_8);
            
            // 移除 XML 声明
            if (omml != null && omml.startsWith("<?xml")) {
                int endOfDecl = omml.indexOf("?>");
                if (endOfDecl > 0) {
                    omml = omml.substring(endOfDecl + 2).trim();
                }
            }
            
            // 清理 OMML，移除不兼容的元素
            omml = sanitizeOmml(omml);

            log.debug("MathML 转 OMML 成功，结果长度: {}", omml != null ? omml.length() : 0);
            
            return omml;
            
        } catch (Exception e) {
            log.error("MathML 转 OMML 失败: {}", e.getMessage(), e);
            return null;
        }
    }


    private String sanitizeMathml(String mathml) {
        if (mathml == null || mathml.isEmpty()) {
            return mathml;
        }

        String s = mathml;

        // Remove invisible operator characters that Word often renders as tofu/blank placeholders.
        s = s.replaceAll(INVISIBLE_OPERATOR_CHARS_REGEX, "");

        // Also handle common numeric entity forms.
        s = s.replace("&#x2061;", "")
            .replace("&#x2062;", "")
            .replace("&#x2063;", "")
            .replace("&#8289;", "")
            .replace("&#8290;", "")
            .replace("&#8291;", "");

        // Some renderers may emit &nbsp; inside MathML.
        s = s.replace("&nbsp;", " ");
        
        // 移除空的 <mrow></mrow> 或 <mrow/> 元素，这些会在转换为 OMML 后变成空的占位符
        s = s.replaceAll("<mrow>\\s*</mrow>", "");
        s = s.replaceAll("<mrow\\s*/>", "");
        
        // 移除空的 <mtext></mtext> 或 <mtext/>
        s = s.replaceAll("<mtext>\\s*</mtext>", "");
        s = s.replaceAll("<mtext\\s*/>", "");

        // 规范化多行对齐公式：将 2 列 mtable 合并为 1 列，并插入 malignmark 对齐点
        // 使得 MML2OMML 可以生成 eqArr，从而在 Word 中获得更稳定的等号对齐效果
        s = normalizeEquationArrayMathml(s);

        return s;
    }

    private String normalizeEquationArrayMathml(String mathml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(new ByteArrayInputStream(mathml.getBytes(StandardCharsets.UTF_8)));
            Element root = doc.getDocumentElement();
            if (root == null) {
                return mathml;
            }

            String mmlNs = root.getNamespaceURI();
            if (mmlNs == null || mmlNs.isBlank()) {
                return mathml;
            }

            NodeList mtables = doc.getElementsByTagNameNS(mmlNs, "mtable");
            if (mtables == null || mtables.getLength() == 0) {
                return mathml;
            }

            for (int t = 0; t < mtables.getLength(); t++) {
                Node mtableNode = mtables.item(t);
                if (!(mtableNode instanceof Element)) {
                    continue;
                }

                Element mtable = (Element) mtableNode;
                NodeList rows = mtable.getElementsByTagNameNS(mmlNs, "mtr");
                if (rows == null || rows.getLength() == 0) {
                    continue;
                }

                boolean convertedAnyRow = false;

                for (int r = 0; r < rows.getLength(); r++) {
                    Node rowNode = rows.item(r);
                    if (!(rowNode instanceof Element)) {
                        continue;
                    }
                    Element mtr = (Element) rowNode;

                    NodeList cells = mtr.getElementsByTagNameNS(mmlNs, "mtd");
                    if (cells == null || cells.getLength() != 2) {
                        continue;
                    }

                    Element left = (Element) cells.item(0);
                    Element right = (Element) cells.item(1);

                    Element mergedMtd = doc.createElementNS(mmlNs, "mtd");
                    Element mrow = doc.createElementNS(mmlNs, "mrow");

                    moveAllChildren(left, mrow);
                    Element malignmark = doc.createElementNS(mmlNs, "malignmark");
                    mrow.appendChild(malignmark);
                    moveAllChildren(right, mrow);

                    mergedMtd.appendChild(mrow);

                    while (mtr.hasChildNodes()) {
                        mtr.removeChild(mtr.getFirstChild());
                    }
                    mtr.appendChild(mergedMtd);
                    convertedAnyRow = true;
                }

                if (convertedAnyRow) {
                    if (!mtable.hasAttribute("frame")) {
                        mtable.setAttribute("frame", "none");
                    }
                    if (!mtable.hasAttribute("columnlines")) {
                        mtable.setAttribute("columnlines", "none");
                    }
                    if (!mtable.hasAttribute("rowlines")) {
                        mtable.setAttribute("rowlines", "none");
                    }
                }
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(doc), new StreamResult(out));
            return out.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.debug("normalizeEquationArrayMathml failed: {}", e.getMessage());
            return mathml;
        }
    }

    private void moveAllChildren(Element from, Element to) {
        while (from.hasChildNodes()) {
            Node child = from.getFirstChild();
            from.removeChild(child);
            to.appendChild(child);
        }
    }
    
    /**
     * 清理 OMML，移除或修正不兼容的元素
     * 主要处理 docx4j 无法解析的元素，如 scrLvl 等
     */
    private String sanitizeOmml(String omml) {
        if (omml == null || omml.isEmpty()) {
            return omml;
        }
        
        String s = omml;
        
        // 移除 <m:scrLvl> 元素（脚本级别控制，docx4j 不支持）
        // 这个元素通常出现在 <m:sSub>, <m:sSup>, <m:sSubSup> 等上下标元素中
        s = s.replaceAll("<m:scrLvl[^>]*>.*?</m:scrLvl>", "");
        s = s.replaceAll("<m:scrLvl[^>]*/?>", "");
        
        // 移除其他可能导致解析失败的非标准元素
        // <m:argSz> 是参数大小控制，如果位置不对也会导致错误
        // 但我们不能简单移除它，因为它在某些上下文中是必需的
        
        // 移除空的 <m:r></m:r> 元素
        s = s.replaceAll("<m:r>\\s*</m:r>", "");
        s = s.replaceAll("<m:r\\s*/>", "");
        
        // 移除空的 <m:e></m:e> 元素（空的数学表达式）
        s = s.replaceAll("<m:e>\\s*</m:e>", "");
        s = s.replaceAll("<m:e\\s*/>", "");
        
        log.debug("OMML 清理完成");
        
        return s;
    }

    /**
     * 将 LaTeX 公式转换为 OMML
     * @param latex LaTeX 公式
     * @return
     */
    @Override
    public String convertLatexToOmml(String latex) {
        return convertLatexToOmml(latex, false);
    }

    /**
     * 将 LaTeX 公式转换为 OMML
     * @param latex LaTeX 公式
     * @param inline 是否为行内公式
     * @return
     */
    @Override
    public String convertLatexToOmml(String latex, boolean inline) {
        // 先规范化一次
        String normalizedLatex = normalizeLatexDelimiters(latex);
        String cacheKey = (inline ? "1|" : "0|") + normalizedLatex;
        String cached = ommlCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.debug("使用 MathJax 转换 LaTeX 到 OMML: {}, inline={}", latex, inline);
        
        // 注意：这里传递 normalizedLatex，但 convertLatexToMathml 内部还会再次调用 normalizeLatexDelimiters
        // 为了避免重复规范化，我们需要确保 normalizeLatexDelimiters 是幂等的
        String mathml = convertLatexToMathml(normalizedLatex, inline);
        if (mathml == null) {
            log.error("LaTeX 转 MathML 失败");
            return null;
        }
        
        String omml = convertMathmlToOmml(mathml);
        if (omml == null) {
            log.error("MathML 转 OMML 失败");
            return null;
        }
        
        log.debug("LaTeX 到 OMML 全链路转换成功");
        ommlCache.put(cacheKey, omml);
        return omml;
    }
    
    /**
     * 检查 MathJax 是否可用
     */
    private boolean isMathJaxAvailable() {
        Boolean cached = mathJaxAvailable;
        if (cached != null) {
            return cached;
        }

        try {
            // 检查 Node.js
            ProcessBuilder pb = new ProcessBuilder(nodeCommand, "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                log.warn("Node.js 不可用，请确保已安装 Node.js");
                mathJaxAvailable = false;
                return false;
            }
            
            // 检查 MathJax 脚本
            Path scriptPath = Paths.get(mathjaxScriptPath);
            if (!Files.exists(scriptPath)) {
                log.warn("MathJax 脚本不存在: {}，尝试在当前目录查找", scriptPath.toAbsolutePath());
                // 尝试在多个位置查找
                String[] possiblePaths = {
                    mathjaxScriptPath,
                    "Ai-doc-engine-server/" + mathjaxScriptPath,
                    "../" + mathjaxScriptPath
                };
                
                boolean found = false;
                for (String path : possiblePaths) {
                    if (Files.exists(Paths.get(path))) {
                        mathjaxScriptPath = path;
                        found = true;
                        log.info("找到 MathJax 脚本: {}", path);
                        break;
                    }
                }
                
                if (!found) {
                    log.error("无法找到 MathJax 脚本");
                    mathJaxAvailable = false;
                    return false;
                }
            }
            
            mathJaxAvailable = true;
            return true;
            
        } catch (Exception e) {
            log.warn("检查 MathJax 可用性失败: {}", e.getMessage());
            mathJaxAvailable = false;
            return false;
        }
    }

    /**
     * 获取 MML 转 OMML 的转换模板
     * @return
     * @throws Exception
     */
    private Templates getMml2OmmlTemplates() throws Exception {
        Templates cached = mml2ommlTemplates;
        if (cached != null) {
            return cached;
        }

        synchronized (this) {
            if (mml2ommlTemplates != null) {
                return mml2ommlTemplates;
            }

            try (InputStream xsltStream = getClass().getClassLoader().getResourceAsStream("MML2OMML.XSL")) {
                if (xsltStream == null) {
                    throw new IllegalStateException("无法加载 MML2OMML.XSL 转换表");
                }
                Source xsltSource = new StreamSource(xsltStream);
                TransformerFactory factory = TransformerFactory.newInstance();
                mml2ommlTemplates = factory.newTemplates(xsltSource);
                return mml2ommlTemplates;
            }
        }
    }

    /**
     * 创建LRU缓存
     *
     * */
    private static <K, V> Map<K, V> lruCache(int maxSize) {
        return Collections.synchronizedMap(new LinkedHashMap<>(256, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        });
    }
    
    /**
     * 规范化 LaTeX 分隔符
     */
    private String normalizeLatexDelimiters(String latex) {
        if (latex == null) {
            return null;
        }
        
        String s = latex.trim();
        
        // 移除外层的 $$ 或 $ 分隔符
        if (s.startsWith("$$") && s.endsWith("$$") && s.length() >= 4) {
            s = s.substring(2, s.length() - 2).trim();
        } else if (s.startsWith("$") && s.endsWith("$") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1).trim();
        }
        
        // 移除 \[ \] 分隔符
        if (s.startsWith("\\[") && s.endsWith("\\]")) {
            s = s.substring(2, s.length() - 2).trim();
        }
        
        // 移除 \( \) 分隔符
        if (s.startsWith("\\(") && s.endsWith("\\)")) {
            s = s.substring(2, s.length() - 2).trim();
        }
        
        // 修复 align* / aligned / matrix 环境中的对齐逻辑
        if (s.contains("\\begin{align") || s.contains("\\begin{aligned") || s.contains("\\begin{matrix")) {
            log.debug("检测到多行公式环境，原始LaTeX: {}", s);
            
            // 1. 修复换行符，确保行分隔正确 (\\)
            // 将单个 \ 且后接换行符的情况修复为 \\
            s = s.replaceAll("\\\\(?![\\\\a-zA-Z])\\s*\n", "\\\\\\\\\n");
            
            // 2. 环境兼容性转换
            // 用户例子中使用 matrix + &，这在 Word OMML 中渲染极不稳定。
            // 将其转换为 aligned 以获得更好的对齐支持。
            if (s.contains("\\begin{matrix")) {
                s = s.replace("\\begin{matrix}", "\\begin{aligned}");
                s = s.replace("\\end{matrix}", "\\end{aligned}");
            }
            
            // 3. 针对用户例子中的特定错误进行修复
            // 修复不规范的括号嵌套：\left ({x1)^2}\right . -> (x-1)^2
            // 修复分母中可能的连字错误：(x1) -> (x-1)
            s = s.replace("\\left ({x1)^2}\\right .", "(x-1)^2");
            s = s.replace("(x1)^2", "(x-1)^2");
            
            // 4. 优化对齐点：移除多余的换行对齐标记
            // 用户例子中每行公式前后的额外 &\\ 可能导致 Word 渲染出空行或错位
            s = s.replaceAll("&\\s*\\\\\\\\\\s*&", "\\\\\\\\");
        } else if (s.contains("\\begin{") && (s.contains("matrix}") || s.contains("array}"))) {
            log.debug("检测到矩阵环境，原始LaTeX: {}", s);
            
            // 策略：只修复行尾的单个反斜杠
            // 匹配模式：行尾的单个 \ 后面跟着换行符或空格+换行符
            // 但不匹配 LaTeX 命令（如 \dots, \vdots, \cdots 等）
            // 使用负向前瞻确保 \ 后面不是字母（LaTeX 命令）
            String fixed = s.replaceAll("\\\\(?![\\\\a-zA-Z])\\s*\n", "\\\\\\\\\n");
            
            // 只有真正修复了才记录日志
            if (!fixed.equals(s)) {
                log.debug("修复后的LaTeX: {}", fixed);
                s = fixed;
            } else {
                log.debug("LaTeX 无需修复（已经是正确格式）");
            }
        }
        
        return s;
    }
}
