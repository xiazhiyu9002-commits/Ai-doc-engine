package com.aidoc.engine.service.impl;

import com.aidoc.engine.config.AppOcrProperties;
import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.model.vo.formula.OcrResponse;
import com.aidoc.engine.model.vo.formula.ValidateResponse;
import com.aidoc.engine.service.FormulaConvertService;
import com.aidoc.engine.service.FormulaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;

/**
 * 公式服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FormulaServiceImpl implements FormulaService {

    private final FormulaConvertService formulaConvertService;
    private final AppOcrProperties appOcrProperties;
    private final RestTemplate ocrRestTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public OcrResponse ocrImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "图片文件不能为空");
        }

        // 检查文件大小（限制 5MB）
        if (image.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED, "图片大小不能超过 5MB");
        }

        // 检查文件类型
        String contentType = image.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "只支持图片文件");
        }

        try {
            String latex;
            BigDecimal confidence;

            String provider = getOcrProvider();
            
            if ("mathpix".equalsIgnoreCase(provider)) {
                MathpixOcrResult result = callMathpixOcr(image);
                latex = result.latex;
                confidence = result.confidence;
            } else if ("custom".equalsIgnoreCase(provider)) {
                CustomOcrResult result = callCustomOcr(image);
                latex = result.latex;
                confidence = result.confidence;
            } else {
                latex = simulateOcr(image);
                confidence = new BigDecimal("0.95");
            }

            // 检测内容类型
            String detectedType = detectContentType(latex);
            
            // 如果是Mermaid流程图，清理markdown标记
            if ("flowchart".equals(detectedType)) {
                latex = cleanMermaidCode(latex);
            }

            return OcrResponse.builder()
                    .latex(latex)
                    .confidence(confidence)
                    .contentType(detectedType)
                    .build();

        } catch (Exception e) {
            log.error("OCR 识别失败", e);
            throw new BusinessException(ErrorCode.FORMULA_OCR_ERROR, "OCR 识别失败: " + e.getMessage());
        }
    }

    private String getOcrProvider() {
        if (appOcrProperties == null) {
            return null;
        }
        return appOcrProperties.getProvider();
    }

    private boolean isMathpixEnabled() {
        if (appOcrProperties == null) {
            return false;
        }
        String provider = appOcrProperties.getProvider();
        if (provider == null || !"mathpix".equalsIgnoreCase(provider.trim())) {
            return false;
        }

        String apiUrl = appOcrProperties.getApiUrl();
        if (apiUrl == null || apiUrl.trim().isEmpty()) {
            return false;
        }

        String appId = appOcrProperties.getAppId();
        String appKey = appOcrProperties.getAppKey();
        if (appKey == null || appKey.trim().isEmpty()) {
            appKey = appOcrProperties.getApiKey();
        }

        return appId != null && !appId.trim().isEmpty() && appKey != null && !appKey.trim().isEmpty();
    }

    private MathpixOcrResult callMathpixOcr(MultipartFile image) throws IOException {
        String apiUrl = appOcrProperties.getApiUrl();
        String appId = appOcrProperties.getAppId();
        String appKey = appOcrProperties.getAppKey();
        if (appKey == null || appKey.trim().isEmpty()) {
            appKey = appOcrProperties.getApiKey();
        }

        Objects.requireNonNull(apiUrl, "Mathpix apiUrl 不能为空");
        Objects.requireNonNull(appId, "Mathpix appId 不能为空");
        Objects.requireNonNull(appKey, "Mathpix appKey 不能为空");

        String contentType = image.getContentType();
        if (contentType == null || contentType.trim().isEmpty()) {
            contentType = "image/png";
        }

        String base64 = Base64.getEncoder().encodeToString(image.getBytes());
        String src = "data:" + contentType + ";base64," + base64;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("src", src);
        payload.put("formats", List.of("latex_simplified", "latex"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("app_id", appId);
        headers.set("app_key", appKey);

        String jsonBody = objectMapper.writeValueAsString(payload);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<String> response = ocrRestTemplate.postForEntity(apiUrl, entity, String.class);
        String body = response.getBody();
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalStateException("Mathpix 响应为空");
        }

        Map<?, ?> result = objectMapper.readValue(body, Map.class);
        String latex = firstNonBlank(asString(result.get("latex_simplified")), asString(result.get("latex")));
        if (latex == null || latex.trim().isEmpty()) {
            throw new IllegalStateException("Mathpix 未返回 LaTeX 字段");
        }

        BigDecimal confidence = toBigDecimal(result.get("latex_confidence"));
        if (confidence == null) {
            confidence = new BigDecimal("0.90");
        }

        return new MathpixOcrResult(latex.trim(), confidence);
    }

    /**
     * 调用自定义 OCR 服务
     */
    private CustomOcrResult callCustomOcr(MultipartFile image) throws IOException {
        String apiUrl = appOcrProperties.getApiUrl();
        
        Objects.requireNonNull(apiUrl, "OCR apiUrl 不能为空");

        try {
            // 使用 multipart/form-data 格式发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // 构建 multipart 请求体
            org.springframework.util.LinkedMultiValueMap<String, Object> body = 
                new org.springframework.util.LinkedMultiValueMap<>();
            body.add("file", image.getResource());
            
            HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> entity = 
                new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = ocrRestTemplate.postForEntity(apiUrl, entity, String.class);
            String responseBody = response.getBody();
            
            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new IllegalStateException("OCR 服务响应为空");
            }

            Map<?, ?> result = objectMapper.readValue(responseBody, Map.class);
            
            // 解析响应数据
            String latex = null;
            BigDecimal confidence = new BigDecimal("0.90");
            
            // 尝试从 data.text 字段获取结果
            Object dataObj = result.get("data");
            if (dataObj instanceof Map) {
                Map<?, ?> data = (Map<?, ?>) dataObj;
                latex = asString(data.get("text"));
                confidence = toBigDecimal(data.get("confidence"));
            }
            
            // 如果没有找到，尝试其他字段
            if (latex == null || latex.trim().isEmpty()) {
                latex = asString(result.get("latex"));
            }
            if (latex == null || latex.trim().isEmpty()) {
                latex = asString(result.get("text"));
            }
            if (latex == null || latex.trim().isEmpty()) {
                latex = asString(result.get("result"));
            }
            
            if (latex == null || latex.trim().isEmpty()) {
                throw new IllegalStateException("OCR 服务未返回有效的 LaTeX 字段");
            }

            // 提取纯LaTeX公式（去掉HTML标签）
            latex = extractLatexFromHtml(latex);

            if (confidence == null) {
                confidence = new BigDecimal("0.90");
            }
            
            return new CustomOcrResult(latex.trim(), confidence);
            
        } catch (Exception e) {
            log.error("调用自定义 OCR 服务失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.OCR_SERVICE_ERROR, "OCR 识别失败: " + e.getMessage());
        }
    }

    private String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v;
            }
        }
        return null;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(s);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从HTML标签中提取LaTeX公式
     * 例如: <div class="formula">$$formula$$</div> -> formula
     */
    private String extractLatexFromHtml(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        // 检测是否为HTML表格
        if (text.contains("<table>") || text.contains("<table ")) {
            return convertHtmlTableToMarkdown(text);
        }
        
        // 移除HTML标签，只保留LaTeX公式
        // 匹配 $$ ... $$ 或 $ ... $
        String result = text.replaceAll("<[^>]+>", "");
        
        // 去掉 $$ 或 $
        result = result.replaceAll("\\$\\$", "").replaceAll("\\$", "").trim();
        
        return result;
    }
    
    /**
     * 将HTML表格转换为Markdown格式
     */
    private String convertHtmlTableToMarkdown(String html) {
        if (html == null || html.trim().isEmpty()) {
            return html;
        }
        
        try {
            StringBuilder markdown = new StringBuilder();
            
            // 提取表格内容
            String tableContent = html;
            if (html.contains("<table>")) {
                int start = html.indexOf("<table");
                int end = html.indexOf("</table>") + 8;
                if (start >= 0 && end > start) {
                    tableContent = html.substring(start, end);
                }
            }
            
            // 解析表格行
            String[] rows = tableContent.split("<tr>|<tr ");
            boolean isFirstRow = true;
            int columnCount = 0;
            
            for (String row : rows) {
                if (!row.contains("<td") && !row.contains("<th")) {
                    continue;
                }
                
                // 提取单元格
                java.util.List<String> cells = new java.util.ArrayList<>();
                String cellPattern = "<t[dh][^>]*>(.*?)</t[dh]>";
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(cellPattern, java.util.regex.Pattern.DOTALL);
                java.util.regex.Matcher matcher = pattern.matcher(row);
                
                while (matcher.find()) {
                    String cellContent = matcher.group(1);
                    // 移除HTML标签
                    cellContent = cellContent.replaceAll("<[^>]+>", "").trim();
                    cells.add(cellContent);
                }
                
                if (cells.isEmpty()) {
                    continue;
                }
                
                // 记录列数
                if (columnCount == 0) {
                    columnCount = cells.size();
                }
                
                // 构建Markdown行
                markdown.append("| ");
                markdown.append(String.join(" | ", cells));
                markdown.append(" |\n");
                
                // 在第一行后添加分隔线
                if (isFirstRow) {
                    markdown.append("| ");
                    for (int i = 0; i < cells.size(); i++) {
                        markdown.append("---");
                        if (i < cells.size() - 1) {
                            markdown.append(" | ");
                        }
                    }
                    markdown.append(" |\n");
                    isFirstRow = false;
                }
            }
            
            return markdown.toString().trim();
            
        } catch (Exception e) {
            log.warn("HTML表格转换失败，返回原始内容: {}", e.getMessage());
            // 如果转换失败，返回移除HTML标签后的内容
            return html.replaceAll("<[^>]+>", "").trim();
        }
    }

    private static class MathpixOcrResult {
        private final String latex;
        private final BigDecimal confidence;

        private MathpixOcrResult(String latex, BigDecimal confidence) {
            this.latex = latex;
            this.confidence = confidence;
        }
    }

    private static class CustomOcrResult {
        private final String latex;
        private final BigDecimal confidence;

        private CustomOcrResult(String latex, BigDecimal confidence) {
            this.latex = latex;
            this.confidence = confidence;
        }
    }

    @Override
    public ValidateResponse validateLatex(String latex) {
        if (latex == null || latex.trim().isEmpty()) {
            return ValidateResponse.builder()
                    .valid(false)
                    .error("LaTeX 公式不能为空")
                    .mathml(null)
                    .build();
        }
        
        try {
            // 尝试转换为 MathML
            String mathml = formulaConvertService.convertLatexToMathml(latex);
            
            return ValidateResponse.builder()
                    .valid(true)
                    .error(null)
                    .mathml(mathml)
                    .build();
            
        } catch (Exception e) {
            log.warn("LaTeX 校验失败: {}", e.getMessage());
            
            return ValidateResponse.builder()
                    .valid(false)
                    .error(e.getMessage())
                    .mathml(null)
                    .build();
        }
    }
    
    /**
     * 模拟 OCR 识别
     * TODO: 替换为真实的 OCR 服务
     */
    private String simulateOcr(MultipartFile image) {
        // 根据文件名返回不同的模拟结果
        String filename = image.getOriginalFilename();

        if (filename != null && filename.contains("frac")) {
            return "\\frac{a}{b}";
        } else if (filename != null && filename.contains("power")) {
            return "x^2";
        } else {
            return "a + b = c";
        }
    }
    
    /**
     * 检测内容类型
     */
    private String detectContentType(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "formula";
        }
        
        String trimmed = content.trim();
        
        // 检测是否为Mermaid流程图
        if (trimmed.contains("```mermaid") || 
            trimmed.startsWith("graph ") || 
            trimmed.startsWith("flowchart ") ||
            trimmed.startsWith("sequenceDiagram") ||
            trimmed.startsWith("classDiagram") ||
            trimmed.startsWith("stateDiagram") ||
            trimmed.startsWith("erDiagram") ||
            trimmed.startsWith("gantt") ||
            trimmed.startsWith("pie") ||
            trimmed.startsWith("journey")) {
            return "flowchart";
        }
        
        // 检测是否为Markdown表格
        if (trimmed.contains("|") && trimmed.contains("---")) {
            String[] lines = trimmed.split("\n");
            if (lines.length >= 2) {
                // 检查是否有表头分隔线
                for (String line : lines) {
                    if (line.trim().matches("^\\|?\\s*[-:]+\\s*(\\|\\s*[-:]+\\s*)+\\|?$")) {
                        return "table";
                    }
                }
            }
        }
        
        return "formula";
    }
    
    /**
     * 清理Mermaid代码，移除markdown标记
     */
    private String cleanMermaidCode(String mermaidCode) {
        if (mermaidCode == null || mermaidCode.trim().isEmpty()) {
            return mermaidCode;
        }
        
        String cleaned = mermaidCode.trim();
        
        // 移除 ```mermaid 和 ```
        cleaned = cleaned.replaceAll("^```mermaid\\s*", "");
        cleaned = cleaned.replaceAll("```\\s*$", "");
        
        return cleaned.trim();
    }
}
