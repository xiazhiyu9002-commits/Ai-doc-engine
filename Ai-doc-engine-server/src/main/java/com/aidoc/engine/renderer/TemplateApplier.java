package com.aidoc.engine.renderer;

import com.aidoc.engine.model.dto.template.TemplateConfig;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

/**
 * 模板应用器
 * 负责将模板配置应用到Word文档
 */
@Slf4j
@Component
public class TemplateApplier {
    
    private final ObjectFactory factory = Context.getWmlObjectFactory();
    
    /**
     * 应用模板配置到Word文档
     */
    public void applyTemplate(WordprocessingMLPackage wordPackage, TemplateConfig config) {
        if (config == null) {
            log.warn("模板配置为空，跳过应用");
            return;
        }
        
        log.info("开始应用模板配置");
        
        try {
            // 应用页面设置
            if (config.getPageSettings() != null) {
                log.info("  ✓ 应用页面设置");
                applyPageSettings(wordPackage, config.getPageSettings());
            } else {
                log.warn("  ✗ 页面设置为空");
            }
            
            // 应用页眉页脚
            if (config.getHeaderFooterSettings() != null) {
                log.info("  ✓ 应用页眉页脚");
                applyHeaderFooter(wordPackage, config.getHeaderFooterSettings());
            } else {
                log.info("  - 页眉页脚未配置");
            }
            
            log.info("模板配置应用完成");
            
        } catch (Exception e) {
            log.error("应用模板配置失败", e);
        }
    }
    
    /**
     * 应用页面设置
     */
    private void applyPageSettings(WordprocessingMLPackage wordPackage, TemplateConfig.PageSettings pageSettings) {
        log.info("应用页面设置: pageSize={}, orientation={}", 
                pageSettings.getPageSize(), pageSettings.getOrientation());
        
        try {
            MainDocumentPart mainDocumentPart = wordPackage.getMainDocumentPart();
            SectPr sectPr = mainDocumentPart.getJaxbElement().getBody().getSectPr();
            
            if (sectPr == null) {
                sectPr = factory.createSectPr();
                mainDocumentPart.getJaxbElement().getBody().setSectPr(sectPr);
            }
            
            // 设置页面大小
            SectPr.PgSz pgSz = sectPr.getPgSz();
            if (pgSz == null) {
                pgSz = factory.createSectPrPgSz();
                sectPr.setPgSz(pgSz);
            }
            
            // A4纸张大小 (210mm x 297mm = 11906 x 16838 twips)
            if ("A4".equalsIgnoreCase(pageSettings.getPageSize())) {
                if ("landscape".equalsIgnoreCase(pageSettings.getOrientation())) {
                    // 横向
                    pgSz.setW(BigInteger.valueOf(16838));
                    pgSz.setH(BigInteger.valueOf(11906));
                    pgSz.setOrient(STPageOrientation.LANDSCAPE);
                } else {
                    // 纵向（默认）
                    pgSz.setW(BigInteger.valueOf(11906));
                    pgSz.setH(BigInteger.valueOf(16838));
                    pgSz.setOrient(STPageOrientation.PORTRAIT);
                }
            }
            
            // 设置页边距
            if (pageSettings.getMargins() != null) {
                applyMargins(sectPr, pageSettings.getMargins());
            }
            
        } catch (Exception e) {
            log.error("应用页面设置失败", e);
        }
    }
    
    /**
     * 应用页边距
     */
    private void applyMargins(SectPr sectPr, TemplateConfig.Margins margins) {
        log.info("应用页边距: top={}, bottom={}, left={}, right={}, gutter={}", 
                margins.getTop(), margins.getBottom(), margins.getLeft(), 
                margins.getRight(), margins.getGutter());
        
        SectPr.PgMar pgMar = sectPr.getPgMar();
        if (pgMar == null) {
            pgMar = factory.createSectPrPgMar();
            sectPr.setPgMar(pgMar);
        }
        
        // 转换厘米到twips (1cm = 567 twips)
        if (margins.getTop() != null) {
            pgMar.setTop(BigInteger.valueOf((long) (margins.getTop() * 567)));
        }
        if (margins.getBottom() != null) {
            pgMar.setBottom(BigInteger.valueOf((long) (margins.getBottom() * 567)));
        }
        if (margins.getLeft() != null) {
            pgMar.setLeft(BigInteger.valueOf((long) (margins.getLeft() * 567)));
        }
        if (margins.getRight() != null) {
            pgMar.setRight(BigInteger.valueOf((long) (margins.getRight() * 567)));
        }
        if (margins.getGutter() != null) {
            pgMar.setGutter(BigInteger.valueOf((long) (margins.getGutter() * 567)));
        }
    }
    
    /**
     * 应用页眉页脚
     */
    private void applyHeaderFooter(WordprocessingMLPackage wordPackage, 
                                   TemplateConfig.HeaderFooterSettings settings) {
        log.info("应用页眉页脚: header={}, footer={}", settings.getHeader(), settings.getFooter());
        
        try {
            MainDocumentPart mainDocumentPart = wordPackage.getMainDocumentPart();
            
            // 页眉
            if (settings.getHeader() != null && !settings.getHeader().isEmpty()) {
                createHeader(wordPackage, mainDocumentPart, settings.getHeader());
            }
            
            // 页脚
            if (settings.getFooter() != null && !settings.getFooter().isEmpty()) {
                createFooter(wordPackage, mainDocumentPart, settings.getFooter());
            }
            
        } catch (Exception e) {
            log.error("应用页眉页脚失败", e);
        }
    }
    
    /**
     * 创建页眉
     */
    private void createHeader(WordprocessingMLPackage wordPackage, MainDocumentPart mainDocumentPart, String headerText) {
        try {
            // 创建页眉部分
            org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart headerPart = 
                new org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart();
            
            // 创建页眉内容
            Hdr hdr = factory.createHdr();
            P paragraph = factory.createP();
            
            // 设置居中对齐
            PPr pPr = factory.createPPr();
            Jc jc = factory.createJc();
            jc.setVal(JcEnumeration.CENTER);
            pPr.setJc(jc);
            paragraph.setPPr(pPr);
            
            // 添加文本
            R run = factory.createR();
            Text text = factory.createText();
            text.setValue(headerText);
            text.setSpace("preserve");
            run.getContent().add(text);
            paragraph.getContent().add(run);
            
            hdr.getContent().add(paragraph);
            headerPart.setJaxbElement(hdr);
            
            // 添加页眉部分到文档
            org.docx4j.relationships.Relationship relationship = 
                mainDocumentPart.addTargetPart(headerPart);
            
            // 在节属性中引用页眉
            SectPr sectPr = mainDocumentPart.getJaxbElement().getBody().getSectPr();
            if (sectPr == null) {
                sectPr = factory.createSectPr();
                mainDocumentPart.getJaxbElement().getBody().setSectPr(sectPr);
            }
            
            HeaderReference headerReference = factory.createHeaderReference();
            headerReference.setId(relationship.getId());
            headerReference.setType(org.docx4j.wml.HdrFtrRef.DEFAULT);
            sectPr.getEGHdrFtrReferences().add(headerReference);
            
            log.info("页眉创建成功");
            
        } catch (Exception e) {
            log.error("创建页眉失败", e);
        }
    }
    
    /**
     * 创建页脚
     */
    private void createFooter(WordprocessingMLPackage wordPackage, MainDocumentPart mainDocumentPart, String footerText) {
        try {
            // 创建页脚部分
            org.docx4j.openpackaging.parts.WordprocessingML.FooterPart footerPart = 
                new org.docx4j.openpackaging.parts.WordprocessingML.FooterPart();
            
            // 创建页脚内容
            Ftr ftr = factory.createFtr();
            P paragraph = factory.createP();
            
            // 设置居中对齐
            PPr pPr = factory.createPPr();
            Jc jc = factory.createJc();
            jc.setVal(JcEnumeration.CENTER);
            pPr.setJc(jc);
            paragraph.setPPr(pPr);
            
            // 处理页码变量
            if (footerText.contains("{page}") || footerText.contains("{total}")) {
                addFooterWithPageNumbers(paragraph, footerText);
            } else {
                // 普通文本
                R run = factory.createR();
                Text text = factory.createText();
                text.setValue(footerText);
                text.setSpace("preserve");
                run.getContent().add(text);
                paragraph.getContent().add(run);
            }
            
            ftr.getContent().add(paragraph);
            footerPart.setJaxbElement(ftr);
            
            // 添加页脚部分到文档
            org.docx4j.relationships.Relationship relationship = 
                mainDocumentPart.addTargetPart(footerPart);
            
            // 在节属性中引用页脚
            SectPr sectPr = mainDocumentPart.getJaxbElement().getBody().getSectPr();
            if (sectPr == null) {
                sectPr = factory.createSectPr();
                mainDocumentPart.getJaxbElement().getBody().setSectPr(sectPr);
            }
            
            FooterReference footerReference = factory.createFooterReference();
            footerReference.setId(relationship.getId());
            footerReference.setType(org.docx4j.wml.HdrFtrRef.DEFAULT);
            sectPr.getEGHdrFtrReferences().add(footerReference);
            
            log.info("页脚创建成功");
            
        } catch (Exception e) {
            log.error("创建页脚失败", e);
        }
    }
    
    /**
     * 添加带页码的页脚
     */
    private void addFooterWithPageNumbers(P paragraph, String footerText) {
        String[] parts = footerText.split("\\{page\\}|\\{total\\}");
        boolean hasPage = footerText.contains("{page}");
        boolean hasTotal = footerText.contains("{total}");
        
        int partIndex = 0;
        int pos = 0;
        
        while (pos < footerText.length()) {
            if (footerText.startsWith("{page}", pos)) {
                // 添加当前页码
                addPageNumberField(paragraph);
                pos += 6;
            } else if (footerText.startsWith("{total}", pos)) {
                // 添加总页数
                addTotalPagesField(paragraph);
                pos += 7;
            } else {
                // 添加普通文本
                if (partIndex < parts.length && !parts[partIndex].isEmpty()) {
                    R run = factory.createR();
                    Text text = factory.createText();
                    text.setValue(parts[partIndex]);
                    text.setSpace("preserve");
                    run.getContent().add(text);
                    paragraph.getContent().add(run);
                }
                pos += parts[partIndex].length();
                partIndex++;
            }
        }
    }
    
    /**
     * 添加当前页码域
     */
    private void addPageNumberField(P paragraph) {
        R run = factory.createR();
        
        // 开始域
        FldChar fldCharBegin = factory.createFldChar();
        fldCharBegin.setFldCharType(STFldCharType.BEGIN);
        run.getContent().add(fldCharBegin);
        paragraph.getContent().add(run);
        
        // 域代码
        R run2 = factory.createR();
        Text text = factory.createText();
        text.setValue("PAGE");
        text.setSpace("preserve");
        run2.getContent().add(text);
        paragraph.getContent().add(run2);
        
        // 结束域
        R run3 = factory.createR();
        FldChar fldCharEnd = factory.createFldChar();
        fldCharEnd.setFldCharType(STFldCharType.END);
        run3.getContent().add(fldCharEnd);
        paragraph.getContent().add(run3);
    }
    
    /**
     * 添加总页数域
     */
    private void addTotalPagesField(P paragraph) {
        R run = factory.createR();
        
        // 开始域
        FldChar fldCharBegin = factory.createFldChar();
        fldCharBegin.setFldCharType(STFldCharType.BEGIN);
        run.getContent().add(fldCharBegin);
        paragraph.getContent().add(run);
        
        // 域代码
        R run2 = factory.createR();
        Text text = factory.createText();
        text.setValue("NUMPAGES");
        text.setSpace("preserve");
        run2.getContent().add(text);
        paragraph.getContent().add(run2);
        
        // 结束域
        R run3 = factory.createR();
        FldChar fldCharEnd = factory.createFldChar();
        fldCharEnd.setFldCharType(STFldCharType.END);
        run3.getContent().add(fldCharEnd);
        paragraph.getContent().add(run3);
    }
}
