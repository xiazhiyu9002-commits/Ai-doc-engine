package com.aidoc.engine.parser.impl;

import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.UdmDocument;
import com.aidoc.engine.model.udm.block.ParagraphBlock;
import com.aidoc.engine.model.udm.content.ParagraphContent;
import com.aidoc.engine.model.udm.content.RichText;
import com.aidoc.engine.service.MermaidParserService;
import com.aidoc.engine.service.impl.MermaidParserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class ParseRichTextDebugTest {

    private ExtendedMarkdownDocumentParser parser;

    @BeforeEach
    void setUp() {
        MermaidParserService mermaidParserService = new MermaidParserServiceImpl();
        parser = new ExtendedMarkdownDocumentParser(mermaidParserService);
    }

    @Test
    void debugParseRichText() {
        String markdown = "**罗尔定理：** 若 $f(x)$ 在 $[a,b]$ 连续";
        
        System.out.println("=== 原始 Markdown ===");
        System.out.println(markdown);
        System.out.println();
        
        UdmDocument doc = parser.parse(markdown);
        
        for (UdmBlock block : doc.getBlocks()) {
            if (block instanceof ParagraphBlock) {
                ParagraphBlock para = (ParagraphBlock) block;
                ParagraphContent content = para.getContent();
                
                System.out.println("=== 解析结果 ===");
                if (content.getSegments() != null) {
                    System.out.println("Segments count: " + content.getSegments().size());
                    for (int i = 0; i < content.getSegments().size(); i++) {
                        RichText seg = content.getSegments().get(i);
                        System.out.println("  [" + i + "] text='" + seg.getText() + "', bold=" + seg.getBold() + ", formula='" + seg.getInlineFormula() + "'");
                    }
                } else {
                    System.out.println("No segments, text: " + content.getText());
                }
            }
        }
    }
}
