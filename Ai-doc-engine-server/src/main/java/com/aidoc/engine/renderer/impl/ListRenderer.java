package com.aidoc.engine.renderer.impl;

import com.aidoc.engine.model.dto.template.TemplateConfig;
import com.aidoc.engine.model.udm.block.ListBlock;
import com.aidoc.engine.model.udm.content.ListContent;
import com.aidoc.engine.model.udm.content.ListItemContent;
import com.aidoc.engine.model.udm.content.RichText;
import com.aidoc.engine.renderer.BlockRenderer;
import com.aidoc.engine.service.FormulaConvertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.math.CTOMath;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.*;
import org.docx4j.XmlUtils;
import org.springframework.stereotype.Component;

import jakarta.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 列表渲染器
 * 将 ListBlock 渲染为 Word 原生列表
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListRenderer implements BlockRenderer<ListBlock> {
    
    private final FormulaConvertService formulaConvertService;
    
    private static final String MATH_NAMESPACE = "http://schemas.openxmlformats.org/officeDocument/2006/math";
    private static final org.docx4j.math.ObjectFactory MATH_FACTORY = new org.docx4j.math.ObjectFactory();
    
    // 用于管理每个文档的编号定义
    private final Map<WordprocessingMLPackage, BigInteger[]> numberingIdsMap = new HashMap<>();
    // 用于管理每个文档的 NumId 计数，支持列表重置序号
    private final Map<WordprocessingMLPackage, AtomicInteger> numIdCounterMap = new HashMap<>();
    
    @Override
    public P render(ListBlock block, WordprocessingMLPackage wordPackage) {
        return render(block, wordPackage, null);
    }
    
    @Override
    public P render(ListBlock block, WordprocessingMLPackage wordPackage, TemplateConfig templateConfig) {
        ListContent content = block.getContent();
        
        if (content == null) {
            log.warn("列表内容为空");
            return null;
        }
        
        // 列表直接添加到文档，返回 null
        renderList(content, wordPackage, 0);
        
        return null;
    }
    
    /**
     * 初始化文档的编号定义
     */
    private BigInteger[] ensureNumbering(WordprocessingMLPackage wordPackage) {
        if (numberingIdsMap.containsKey(wordPackage)) {
            return numberingIdsMap.get(wordPackage);
        }

        try {
            NumberingDefinitionsPart numberingPart = wordPackage.getMainDocumentPart().getNumberingDefinitionsPart();
            if (numberingPart == null) {
                numberingPart = new NumberingDefinitionsPart();
                wordPackage.getMainDocumentPart().addTargetPart(numberingPart);
            }

            // 创建编号定义
            Numbering numbering = numberingPart.getContents();
            if (numbering == null) {
                numbering = new ObjectFactory().createNumbering();
                numberingPart.setJaxbElement(numbering);
            }

            // 1. 定义有序列表 (AbstractNumId = 1, NumId = 1)
            BigInteger orderedAbstractId = BigInteger.valueOf(1);
            if (!hasAbstractNum(numbering, orderedAbstractId)) {
                numbering.getAbstractNum().add(createAbstractNum(orderedAbstractId, NumberFormat.DECIMAL, "%1."));
            }
            BigInteger orderedNumId = BigInteger.valueOf(1);
            if (!hasNum(numbering, orderedNumId)) {
                numbering.getNum().add(createNum(orderedNumId, orderedAbstractId));
            }

            // 2. 定义无序列表 (AbstractNumId = 2, NumId = 2)
            BigInteger unorderedAbstractId = BigInteger.valueOf(2);
            if (!hasAbstractNum(numbering, unorderedAbstractId)) {
                numbering.getAbstractNum().add(createAbstractNum(unorderedAbstractId, NumberFormat.BULLET, "•"));
            }
            BigInteger unorderedNumId = BigInteger.valueOf(2);
            if (!hasNum(numbering, unorderedNumId)) {
                numbering.getNum().add(createNum(unorderedNumId, unorderedAbstractId));
            }

            BigInteger[] ids = new BigInteger[]{orderedNumId, unorderedNumId};
            numberingIdsMap.put(wordPackage, ids);
            return ids;
        } catch (Exception e) {
            log.error("初始化编号定义失败", e);
            return new BigInteger[]{BigInteger.ONE, BigInteger.valueOf(2)};
        }
    }

    private boolean hasAbstractNum(Numbering numbering, BigInteger id) {
        return numbering.getAbstractNum().stream().anyMatch(a -> a.getAbstractNumId().equals(id));
    }

    private boolean hasNum(Numbering numbering, BigInteger id) {
        return numbering.getNum().stream().anyMatch(n -> n.getNumId().equals(id));
    }

    private Numbering.AbstractNum createAbstractNum(BigInteger id, NumberFormat fmt, String lvlText) {
        ObjectFactory factory = new ObjectFactory();
        Numbering.AbstractNum abstractNum = factory.createNumberingAbstractNum();
        abstractNum.setAbstractNumId(id);
        
        // 定义 0-8 级
        for (int i = 0; i < 9; i++) {
            Lvl lvl = factory.createLvl();
            lvl.setIlvl(BigInteger.valueOf(i));
            lvl.setStart(factory.createLvlStart());
            lvl.getStart().setVal(BigInteger.ONE);
            
            NumFmt numFmt = factory.createNumFmt();
            numFmt.setVal(fmt);
            lvl.setNumFmt(numFmt);
            
            Lvl.LvlText text = factory.createLvlLvlText();
            text.setVal(lvlText.replace("%1", "%" + (i + 1)));
            lvl.setLvlText(text);
            
            // 缩进设置
            PPr pPr = factory.createPPr();
            PPrBase.Ind ind = factory.createPPrBaseInd();
            ind.setLeft(BigInteger.valueOf(720 * (i + 1)));
            ind.setHanging(BigInteger.valueOf(360));
            pPr.setInd(ind);
            lvl.setPPr(pPr);
            
            abstractNum.getLvl().add(lvl);
        }
        return abstractNum;
    }

    private Numbering.Num createNum(BigInteger numId, BigInteger abstractNumId) {
        ObjectFactory factory = new ObjectFactory();
        Numbering.Num num = factory.createNumberingNum();
        num.setNumId(numId);
        Numbering.Num.AbstractNumId absId = factory.createNumberingNumAbstractNumId();
        absId.setVal(abstractNumId);
        num.setAbstractNumId(absId);
        return num;
    }

    /**
     * 为当前列表获取一个新的 NumId（用于重置序号）
     */
    private BigInteger getNextNumId(WordprocessingMLPackage wordPackage, boolean ordered) {
        ensureNumbering(wordPackage);
        
        // 关键修复：每次都创建一个全新的 AbstractNum 以彻底切断与之前列表的联系
        AtomicInteger counter = numIdCounterMap.computeIfAbsent(wordPackage, k -> new AtomicInteger(100));
        BigInteger newAbstractId = BigInteger.valueOf(counter.incrementAndGet());
        BigInteger newNumId = BigInteger.valueOf(counter.incrementAndGet());

        try {
            NumberingDefinitionsPart numberingPart = wordPackage.getMainDocumentPart().getNumberingDefinitionsPart();
            Numbering numbering = numberingPart.getContents();
            
            // 创建一个新的 AbstractNum 定义
            NumberFormat format = ordered ? NumberFormat.DECIMAL : NumberFormat.BULLET;
            String lvlText = ordered ? "%1." : "•";
            numbering.getAbstractNum().add(createAbstractNum(newAbstractId, format, lvlText));
            
            // 将新的 NumId 关联到这个新的 AbstractNum
            numbering.getNum().add(createNum(newNumId, newAbstractId));
            
            log.info("为列表块创建了独立编号定义: NumId={}, AbstractNumId={}, Type={}", 
                newNumId, newAbstractId, ordered ? "Ordered" : "Unordered");
        } catch (Exception e) {
            log.error("创建新的 NumId 失败", e);
        }
        
        return newNumId;
    }

    /**
     * 渲染列表（支持嵌套）
     */
    private void renderList(ListContent content, WordprocessingMLPackage wordPackage, int level) {
        ObjectFactory factory = new ObjectFactory();
        
        // 如果是顶层列表（level == 0），为该列表分配一个新的 NumId 以重置序号
        BigInteger numId;
        if (level == 0) {
            numId = getNextNumId(wordPackage, content.getOrdered());
        } else {
            // 嵌套列表使用父列表相同的逻辑，或者根据需要分配
            BigInteger[] baseIds = ensureNumbering(wordPackage);
            numId = content.getOrdered() ? baseIds[0] : baseIds[1];
        }
        
        // 获取列表项
        if (content.hasRichItems()) {
            for (ListItemContent item : content.getItemContents()) {
                renderListItem(item, wordPackage, factory, level, numId);
            }
        } else if (content.getItems() != null && !content.getItems().isEmpty()) {
            // 简单模式
            for (String itemText : content.getItems()) {
                P paragraph = createListParagraph(itemText, factory, level, numId);
                wordPackage.getMainDocumentPart().getContent().add(paragraph);
            }
        }
    }

    /**
     * 渲染列表项
     */
    private void renderListItem(ListItemContent item, WordprocessingMLPackage wordPackage, 
            ObjectFactory factory, int level, BigInteger numIdValue) {
        
        // 创建列表项段落
        P paragraph = factory.createP();
        
        // 设置段落属性（缩进已在 AbstractNum 中定义，但此处保留以防万一）
        PPr pPr = factory.createPPr();
        PPrBase.Ind ind = factory.createPPrBaseInd();
        ind.setLeft(BigInteger.valueOf(720 * (level + 1)));
        ind.setHanging(BigInteger.valueOf(360));
        pPr.setInd(ind);
        
        // 设置编号格式
        PPrBase.NumPr numPr = factory.createPPrBaseNumPr();
        PPrBase.NumPr.NumId numId = factory.createPPrBaseNumPrNumId();
        numId.setVal(numIdValue);
        numPr.setNumId(numId);
        PPrBase.NumPr.Ilvl ilvl = factory.createPPrBaseNumPrIlvl();
        ilvl.setVal(BigInteger.valueOf(level));
        numPr.setIlvl(ilvl);
        pPr.setNumPr(numPr);
        
        paragraph.setPPr(pPr);
        
        // 渲染富文本内容
        for (RichText segment : item.getSegments()) {
            Object runOrMath = createRichTextContent(segment, factory);
            paragraph.getContent().add(runOrMath);
        }
        
        wordPackage.getMainDocumentPart().getContent().add(paragraph);
        
        // 递归渲染嵌套列表
        if (item.getNestedList() != null) {
            renderList(item.getNestedList(), wordPackage, level + 1);
        }
    }
    
    /**
     * 创建简单列表段落
     */
    private P createListParagraph(String text, ObjectFactory factory, int level, BigInteger numIdValue) {
        P paragraph = factory.createP();
        
        // 设置段落属性
        PPr pPr = factory.createPPr();
        PPrBase.Ind ind = factory.createPPrBaseInd();
        ind.setLeft(BigInteger.valueOf(720 * (level + 1)));
        ind.setHanging(BigInteger.valueOf(360));
        pPr.setInd(ind);
        
        // 设置编号格式
        PPrBase.NumPr numPr = factory.createPPrBaseNumPr();
        PPrBase.NumPr.NumId numId = factory.createPPrBaseNumPrNumId();
        numId.setVal(numIdValue);
        numPr.setNumId(numId);
        PPrBase.NumPr.Ilvl ilvl = factory.createPPrBaseNumPrIlvl();
        ilvl.setVal(BigInteger.valueOf(level));
        numPr.setIlvl(ilvl);
        pPr.setNumPr(numPr);
        
        paragraph.setPPr(pPr);
        
        // 添加文本
        R run = factory.createR();
        Text textElement = factory.createText();
        textElement.setValue(text != null ? text : "");
        textElement.setSpace("preserve");
        run.getContent().add(textElement);
        paragraph.getContent().add(run);
        
        return paragraph;
    }
    
    /**
     * 创建富文本内容（支持行内公式）
     */
    private Object createRichTextContent(RichText segment, ObjectFactory factory) {
        if (segment.getInlineFormula() != null && !segment.getInlineFormula().isEmpty()) {
            return createInlineFormulaRun(segment, factory);
        }
        
        return createRichTextRun(segment, factory);
    }
    
    /**
     * 创建富文本运行
     */
    private R createRichTextRun(RichText segment, ObjectFactory factory) {
        R run = factory.createR();
        
        // 设置格式属性
        RPr rPr = factory.createRPr();
        boolean hasFormat = false;
        
        if (Boolean.TRUE.equals(segment.getBold())) {
            rPr.setB(factory.createBooleanDefaultTrue());
            hasFormat = true;
        }
        
        if (Boolean.TRUE.equals(segment.getItalic())) {
            rPr.setI(factory.createBooleanDefaultTrue());
            hasFormat = true;
        }
        
        if (Boolean.TRUE.equals(segment.getStrikethrough())) {
            rPr.setStrike(factory.createBooleanDefaultTrue());
            hasFormat = true;
        }
        
        if (Boolean.TRUE.equals(segment.getCode())) {
            // 行内代码使用等宽字体
            RFonts fonts = factory.createRFonts();
            fonts.setAscii("Consolas");
            fonts.setHAnsi("Consolas");
            rPr.setRFonts(fonts);
            hasFormat = true;
        }
        
        if (hasFormat) {
            run.setRPr(rPr);
        }
        
        // 添加文本
        String textContent = segment.getText();
        if (textContent != null && !textContent.isEmpty()) {
            // 处理换行符：如果文本只是换行符，添加 <w:br/> 元素
            if ("\n".equals(textContent)) {
                Br br = factory.createBr();
                run.getContent().add(br);
            } else {
                Text text = factory.createText();
                text.setValue(textContent);
                text.setSpace("preserve");
                run.getContent().add(text);
            }
        }
        
        return run;
    }
    
    /**
     * 创建行内公式运行
     */
    private R createInlineFormulaRun(RichText segment, ObjectFactory factory) {
        String latex = segment.getInlineFormula();
        log.info("渲染列表项中的行内公式: {}", latex);
        
        R run = factory.createR();
        
        try {
            String ommlString = formulaConvertService.convertLatexToOmml(latex, true);
            
            if (ommlString == null || ommlString.trim().isEmpty()) {
                log.warn("行内公式转换失败，降级为文本: {}", latex);
                return createFallbackFormulaRun(latex, factory);
            }
            
            CTOMath omath = parseOmmlToCTOMath(ommlString);
            if (omath == null) {
                log.warn("行内公式 OMML 解析失败，降级为文本: {}", latex);
                return createFallbackFormulaRun(latex, factory);
            }
            
            JAXBElement<CTOMath> omathElement = MATH_FACTORY.createOMath(omath);
            run.getContent().add(omathElement);
            
            log.info("列表项行内公式渲染成功: {}", latex);
            return run;
            
        } catch (Exception e) {
            log.error("列表项行内公式渲染异常: {}", e.getMessage(), e);
            return createFallbackFormulaRun(latex, factory);
        }
    }
    
    /**
     * 解析 OMML 字符串为 CTOMath 对象
     */
    private CTOMath parseOmmlToCTOMath(String ommlString) {
        try {
            String wrappedOmml = ommlString;
            
            if (!ommlString.contains("<m:oMath") && !ommlString.contains("<oMath")) {
                wrappedOmml = "<m:oMath xmlns:m=\"" + MATH_NAMESPACE + "\">" + ommlString + "</m:oMath>";
            }
            
            if (!wrappedOmml.contains("xmlns:m=")) {
                wrappedOmml = wrappedOmml.replace("<m:oMath", "<m:oMath xmlns:m=\"" + MATH_NAMESPACE + "\"");
            }
            
            log.debug("准备解析 OMML，长度: {}", wrappedOmml.length());
            
            Object unmarshalled = XmlUtils.unmarshalString(wrappedOmml);
            
            if (unmarshalled instanceof CTOMath) {
                log.debug("OMML 解析成功（直接类型）");
                return (CTOMath) unmarshalled;
            } else if (unmarshalled instanceof JAXBElement) {
                JAXBElement<?> jaxbElement = (JAXBElement<?>) unmarshalled;
                Object value = jaxbElement.getValue();
                if (value instanceof CTOMath) {
                    log.debug("OMML 解析成功（JAXBElement 包装）");
                    return (CTOMath) value;
                }
            }
            
            log.error("无法将 OMML 解析为 CTOMath，实际类型: {}", 
                unmarshalled != null ? unmarshalled.getClass().getName() : "null");
            return null;
            
        } catch (Exception e) {
            log.error("解析 OMML 到 CTOMath 异常: {}", e.getMessage());
            log.debug("失败的 OMML 内容（前500字符）: {}", 
                ommlString != null && ommlString.length() > 500 
                    ? ommlString.substring(0, 500) + "..." 
                    : ommlString);
            return null;
        }
    }
    
    /**
     * 创建降级公式运行（转换失败时使用）
     */
    private R createFallbackFormulaRun(String latex, ObjectFactory factory) {
        R run = factory.createR();
        RPr rPr = factory.createRPr();
        
        BooleanDefaultTrue italic = factory.createBooleanDefaultTrue();
        rPr.setI(italic);
        
        Color color = factory.createColor();
        color.setVal("0066CC");
        rPr.setColor(color);
        
        run.setRPr(rPr);
        
        Text text = factory.createText();
        text.setValue("$" + latex + "$");
        text.setSpace("preserve");
        run.getContent().add(text);
        
        return run;
    }
}
