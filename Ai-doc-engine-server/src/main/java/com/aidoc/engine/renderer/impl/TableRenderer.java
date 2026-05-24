package com.aidoc.engine.renderer.impl;

import com.aidoc.engine.model.dto.template.TemplateConfig;
import com.aidoc.engine.model.udm.block.TableBlock;
import com.aidoc.engine.model.udm.content.RichText;
import com.aidoc.engine.model.udm.content.TableContent;
import com.aidoc.engine.renderer.BlockRenderer;
import com.aidoc.engine.service.FormulaConvertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.jaxb.Context;
import org.docx4j.math.CTOMath;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.docx4j.XmlUtils;
import org.springframework.stereotype.Component;

import jakarta.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.List;

/**
 * 表格渲染器
 * 将 TableBlock 渲染为 Word 原生表格（w:tbl），支持富文本单元格
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TableRenderer implements BlockRenderer<TableBlock> {
    
    private final FormulaConvertService formulaConvertService;
    
    private static final String MATH_NAMESPACE = "http://schemas.openxmlformats.org/officeDocument/2006/math";
    private static final org.docx4j.math.ObjectFactory MATH_FACTORY = new org.docx4j.math.ObjectFactory();
    
    @Override
    public P render(TableBlock block, WordprocessingMLPackage wordPackage) {
        return render(block, wordPackage, null);
    }
    
    @Override
    public P render(TableBlock block, WordprocessingMLPackage wordPackage, TemplateConfig templateConfig) {
        TableContent content = block.getContent();
        
        if (content == null) {
            log.warn("表格内容为空");
            return null;
        }
        
        // 检查是否有内容
        boolean hasHeaders = content.getHeaders() != null && !content.getHeaders().isEmpty();
        boolean hasHeaderCells = content.getHeaderCells() != null && !content.getHeaderCells().isEmpty();
        
        if (!hasHeaders && !hasHeaderCells) {
            log.warn("表格内容为空");
            return null;
        }
        
        try {
            // 创建表格
            Tbl table = createTable(content);
            
            // 将表格添加到文档
            wordPackage.getMainDocumentPart().getContent().add(table);
            
            log.debug("渲染表格: headers={}, rows={}", 
                    content.getHeaders() != null ? content.getHeaders().size() : 0,
                    content.getRows() != null ? content.getRows().size() : 0);
            
            return null;
            
        } catch (Exception e) {
            log.error("表格渲染失败", e);
            return null;
        }
    }
    
    /**
     * 创建 Word 表格
     */
    private Tbl createTable(TableContent content) {
        ObjectFactory factory = Context.getWmlObjectFactory();
        
        Tbl table = factory.createTbl();
        
        // 设置表格属性
        TblPr tblPr = factory.createTblPr();
        
        // 设置表格边框
        TblBorders borders = factory.createTblBorders();
        CTBorder border = factory.createCTBorder();
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(4));
        border.setSpace(BigInteger.ZERO);
        border.setColor("000000");
        
        borders.setTop(border);
        borders.setBottom(border);
        borders.setLeft(border);
        borders.setRight(border);
        borders.setInsideH(border);
        borders.setInsideV(border);
        
        tblPr.setTblBorders(borders);
        
        // 设置表格宽度
        TblWidth tblWidth = factory.createTblWidth();
        tblWidth.setType("auto");
        tblWidth.setW(BigInteger.valueOf(0));
        tblPr.setTblW(tblWidth);
        
        table.setTblPr(tblPr);
        
        // 计算列数
        int colCount = getTableColumnCount(content);
        
        // 创建表格网格
        TblGrid tblGrid = factory.createTblGrid();
        for (int i = 0; i < colCount; i++) {
            TblGridCol gridCol = factory.createTblGridCol();
            gridCol.setW(BigInteger.valueOf(2000));
            tblGrid.getGridCol().add(gridCol);
        }
        table.setTblGrid(tblGrid);
        
        // 添加表头行
        log.info("表格渲染 - hasRichCells: {}, headerCells: {}, headers: {}", 
                content.hasRichCells(), 
                content.getHeaderCells() != null ? content.getHeaderCells().size() : "null",
                content.getHeaders() != null ? content.getHeaders().size() : "null");
        
        if (content.getHeaderCells() != null && !content.getHeaderCells().isEmpty()) {
            log.info("使用 headerCells 渲染表头，数据: {}", content.getHeaderCells());
        }
        if (content.getHeaders() != null && !content.getHeaders().isEmpty()) {
            log.info("headers 数据: {}", content.getHeaders());
        }
        
        // 修复：headerCells 是 List<List<RichText>>，表示一行中的多个单元格
        // 每个 List<RichText> 是一个单元格的内容（可能包含多个 RichText 段）
        if (content.hasRichCells() && content.getHeaderCells() != null && !content.getHeaderCells().isEmpty()) {
            // 使用富文本表头 - headerCells 是一行的多个单元格，每个单元格可能有多个段
            log.info("使用富文本表头，单元格数: {}", content.getHeaderCells().size());
            Tr headerRow = createRichTableRowMultiSegment(content.getHeaderCells(), factory, true);
            table.getContent().add(headerRow);
        } else if (content.getHeaders() != null && !content.getHeaders().isEmpty()) {
            // 使用简单文本表头
            log.info("使用简单文本表头");
            Tr headerRow = createTableRow(content.getHeaders(), factory, true);
            table.getContent().add(headerRow);
        }
        
        // 添加数据行
        if (content.hasRichCells() && content.getRowCells() != null) {
            for (List<List<RichText>> rowCells : content.getRowCells()) {
                Tr dataRow = createRichTableRowMultiSegment(rowCells, factory, false);
                table.getContent().add(dataRow);
            }
        } else if (content.getRows() != null) {
            for (List<String> rowData : content.getRows()) {
                Tr dataRow = createTableRow(rowData, factory, false);
                table.getContent().add(dataRow);
            }
        }
        
        return table;
    }
    
    /**
     * 获取表格列数
     */
    private int getTableColumnCount(TableContent content) {
        if (content.hasRichCells() && content.getHeaderCells() != null && !content.getHeaderCells().isEmpty()) {
            return content.getHeaderCells().size();
        }
        if (content.getHeaders() != null && !content.getHeaders().isEmpty()) {
            return content.getHeaders().size();
        }
        if (content.getRows() != null && !content.getRows().isEmpty()) {
            return content.getRows().get(0).size();
        }
        return 1;
    }
    
    /**
     * 创建简单表格行
     */
    private Tr createTableRow(List<String> cellData, ObjectFactory factory, boolean isHeader) {
        Tr row = factory.createTr();
        
        for (String cellText : cellData) {
            Tc cell = createTableCell(cellText, factory, isHeader);
            row.getContent().add(cell);
        }
        
        return row;
    }
    
    /**
     * 创建富文本表格行（表头用，每个单元格是单个 RichText）
     */
    private Tr createRichTableRow(List<RichText> cellData, ObjectFactory factory, boolean isHeader) {
        Tr row = factory.createTr();
        
        for (RichText cellContent : cellData) {
            Tc cell = createRichTableCell(cellContent, factory, isHeader);
            row.getContent().add(cell);
        }
        
        return row;
    }
    
    /**
     * 创建富文本表格行（数据行用，每个单元格包含多个 RichText 段）
     */
    private Tr createRichTableRowMultiSegment(List<List<RichText>> rowCells, ObjectFactory factory, boolean isHeader) {
        Tr row = factory.createTr();
        
        for (List<RichText> cellSegments : rowCells) {
            Tc cell = createRichTableCellMultiSegment(cellSegments, factory, isHeader);
            row.getContent().add(cell);
        }
        
        return row;
    }
    
    /**
     * 创建简单表格单元格
     */
    private Tc createTableCell(String text, ObjectFactory factory, boolean isHeader) {
        Tc cell = factory.createTc();
        
        // 设置单元格属性
        TcPr tcPr = factory.createTcPr();
        
        // 设置单元格边框
        TcPrInner.TcBorders tcBorders = factory.createTcPrInnerTcBorders();
        CTBorder border = factory.createCTBorder();
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(4));
        border.setSpace(BigInteger.ZERO);
        border.setColor("000000");
        
        tcBorders.setTop(border);
        tcBorders.setBottom(border);
        tcBorders.setLeft(border);
        tcBorders.setRight(border);
        
        tcPr.setTcBorders(tcBorders);
        
        // 如果是表头，设置背景色
        if (isHeader) {
            CTShd shd = factory.createCTShd();
            shd.setVal(STShd.CLEAR);
            shd.setColor("auto");
            shd.setFill("D9D9D9");
            tcPr.setShd(shd);
        }
        
        cell.setTcPr(tcPr);
        
        // 创建段落
        P paragraph = factory.createP();
        
        // 创建文本运行
        R run = factory.createR();
        
        // 如果是表头，设置加粗
        if (isHeader) {
            RPr rPr = factory.createRPr();
            BooleanDefaultTrue bold = factory.createBooleanDefaultTrue();
            bold.setVal(true);
            rPr.setB(bold);
            run.setRPr(rPr);
        }
        
        Text textElement = factory.createText();
        textElement.setValue(text != null ? text : "");
        textElement.setSpace("preserve");
        run.getContent().add(textElement);
        
        paragraph.getContent().add(run);
        cell.getContent().add(paragraph);
        
        return cell;
    }
    
    /**
     * 创建富文本表格单元格（单个 RichText）
     */
    private Tc createRichTableCell(RichText cellContent, ObjectFactory factory, boolean isHeader) {
        Tc cell = factory.createTc();
        
        // 设置单元格属性
        TcPr tcPr = factory.createTcPr();
        
        // 设置单元格边框
        TcPrInner.TcBorders tcBorders = factory.createTcPrInnerTcBorders();
        CTBorder border = factory.createCTBorder();
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(4));
        border.setSpace(BigInteger.ZERO);
        border.setColor("000000");
        
        tcBorders.setTop(border);
        tcBorders.setBottom(border);
        tcBorders.setLeft(border);
        tcBorders.setRight(border);
        
        tcPr.setTcBorders(tcBorders);
        
        // 如果是表头，设置背景色
        if (isHeader) {
            CTShd shd = factory.createCTShd();
            shd.setVal(STShd.CLEAR);
            shd.setColor("auto");
            shd.setFill("D9D9D9");
            tcPr.setShd(shd);
        }
        
        cell.setTcPr(tcPr);
        
        // 创建段落
        P paragraph = factory.createP();
        
        // 创建富文本运行
        R run = createRichTextRun(cellContent, factory, isHeader);
        paragraph.getContent().add(run);
        
        cell.getContent().add(paragraph);
        
        return cell;
    }
    
    /**
     * 创建富文本表格单元格（多个 RichText 段）
     */
    private Tc createRichTableCellMultiSegment(List<RichText> segments, ObjectFactory factory, boolean isHeader) {
        Tc cell = factory.createTc();
        
        // 设置单元格属性
        TcPr tcPr = factory.createTcPr();
        
        // 设置单元格边框
        TcPrInner.TcBorders tcBorders = factory.createTcPrInnerTcBorders();
        CTBorder border = factory.createCTBorder();
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(4));
        border.setSpace(BigInteger.ZERO);
        border.setColor("000000");
        
        tcBorders.setTop(border);
        tcBorders.setBottom(border);
        tcBorders.setLeft(border);
        tcBorders.setRight(border);
        
        tcPr.setTcBorders(tcBorders);
        
        // 如果是表头，设置背景色
        if (isHeader) {
            CTShd shd = factory.createCTShd();
            shd.setVal(STShd.CLEAR);
            shd.setColor("auto");
            shd.setFill("D9D9D9");
            tcPr.setShd(shd);
        }
        
        cell.setTcPr(tcPr);
        
        // 创建段落
        P paragraph = factory.createP();
        
        // 为每个 RichText 段创建运行
        if (segments != null && !segments.isEmpty()) {
            for (RichText segment : segments) {
                R run = createRichTextRun(segment, factory, isHeader);
                paragraph.getContent().add(run);
            }
        }
        
        cell.getContent().add(paragraph);
        
        return cell;
    }
    
    /**
     * 创建富文本运行
     */
    private R createRichTextRun(RichText segment, ObjectFactory factory, boolean isHeader) {
        // 检查是否为行内公式
        if (segment.getInlineFormula() != null && !segment.getInlineFormula().isEmpty()) {
            return createInlineFormulaRun(segment, factory);
        }
        
        // 检查是否为换行符
        if ("\n".equals(segment.getText())) {
            R run = factory.createR();
            Br br = factory.createBr();
            run.getContent().add(br);
            return run;
        }
        
        R run = factory.createR();
        
        // 设置格式属性
        RPr rPr = factory.createRPr();
        boolean hasFormat = false;
        
        // 表头默认加粗
        if (isHeader) {
            rPr.setB(factory.createBooleanDefaultTrue());
            hasFormat = true;
        }
        
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
            RFonts fonts = factory.createRFonts();
            fonts.setAscii("Consolas");
            fonts.setHAnsi("Consolas");
            fonts.setCs("Consolas");
            rPr.setRFonts(fonts);
            hasFormat = true;
        }
        
        if (hasFormat) {
            run.setRPr(rPr);
        }
        
        // 添加文本
        String textContent = segment.getText();
        if (textContent != null && !textContent.isEmpty()) {
            Text text = factory.createText();
            text.setValue(textContent);
            text.setSpace("preserve");
            run.getContent().add(text);
        }
        
        return run;
    }
    
    /**
     * 创建行内公式运行
     */
    private R createInlineFormulaRun(RichText segment, ObjectFactory factory) {
        String latex = segment.getInlineFormula();
        log.info("表格中渲染行内公式: {}", latex);
        
        R run = factory.createR();
        
        try {
            // 使用 FormulaConvertService 转换公式
            String ommlString = formulaConvertService.convertLatexToOmml(latex, true);
            
            if (ommlString == null || ommlString.trim().isEmpty()) {
                log.warn("表格行内公式转换失败，降级为文本: {}", latex);
                return createFallbackFormulaRun(latex, factory);
            }
            
            // 解析 OMML 为 CTOMath
            CTOMath omath = parseOmmlToCTOMath(ommlString);
            if (omath == null) {
                log.warn("表格行内公式 OMML 解析失败，降级为文本: {}", latex);
                return createFallbackFormulaRun(latex, factory);
            }
            
            // 创建 JAXBElement 并添加到运行中
            JAXBElement<CTOMath> omathElement = MATH_FACTORY.createOMath(omath);
            run.getContent().add(omathElement);
            
            log.info("表格行内公式渲染成功: {}", latex);
            return run;
            
        } catch (Exception e) {
            log.error("表格行内公式渲染异常: {}", e.getMessage(), e);
            return createFallbackFormulaRun(latex, factory);
        }
    }
    
    /**
     * 解析 OMML 字符串为 CTOMath 对象
     */
    private CTOMath parseOmmlToCTOMath(String ommlString) {
        try {
            String wrappedOmml = ommlString;
            
            // 确保有 m:oMath 根元素
            if (!ommlString.contains("<m:oMath") && !ommlString.contains("<oMath")) {
                wrappedOmml = "<m:oMath xmlns:m=\"" + MATH_NAMESPACE + "\">" + ommlString + "</m:oMath>";
            }
            
            // 确保有命名空间声明
            if (!wrappedOmml.contains("xmlns:m=")) {
                wrappedOmml = wrappedOmml.replace("<m:oMath", "<m:oMath xmlns:m=\"" + MATH_NAMESPACE + "\"");
            }
            
            // 解析 XML
            Object unmarshalled = XmlUtils.unmarshalString(wrappedOmml);
            
            if (unmarshalled instanceof CTOMath) {
                return (CTOMath) unmarshalled;
            } else if (unmarshalled instanceof JAXBElement) {
                JAXBElement<?> jaxbElement = (JAXBElement<?>) unmarshalled;
                Object value = jaxbElement.getValue();
                if (value instanceof CTOMath) {
                    return (CTOMath) value;
                }
            }
            
            log.error("无法将 OMML 解析为 CTOMath，实际类型: {}", 
                unmarshalled != null ? unmarshalled.getClass().getName() : "null");
            return null;
            
        } catch (Exception e) {
            log.error("解析 OMML 到 CTOMath 异常: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 创建降级显示的公式运行（当转换失败时）
     */
    private R createFallbackFormulaRun(String latex, ObjectFactory factory) {
        R run = factory.createR();
        RPr rPr = factory.createRPr();
        
        // 设置斜体和颜色
        BooleanDefaultTrue italic = factory.createBooleanDefaultTrue();
        rPr.setI(italic);
        
        Color color = factory.createColor();
        color.setVal("0066CC");
        rPr.setColor(color);
        
        run.setRPr(rPr);
        
        // 添加文本
        Text text = factory.createText();
        text.setValue("$" + latex + "$");
        text.setSpace("preserve");
        run.getContent().add(text);
        
        return run;
    }
}
