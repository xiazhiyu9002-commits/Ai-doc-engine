package com.aidoc.engine.parser.impl;

import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.UdmDocument;
import com.aidoc.engine.model.udm.block.FormulaBlock;
import com.aidoc.engine.model.udm.block.ParagraphBlock;
import com.aidoc.engine.model.udm.content.ParagraphContent;
import com.aidoc.engine.model.udm.content.RichText;
import com.aidoc.engine.service.MermaidParserService;
import com.aidoc.engine.service.impl.MermaidParserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserReportedFormulaTest {

    private ExtendedMarkdownDocumentParser parser;

    @BeforeEach
    void setUp() {
        MermaidParserService mermaidParserService = new MermaidParserServiceImpl();
        parser = new ExtendedMarkdownDocumentParser(mermaidParserService);
    }

    @Test
    void testFormulaWithBrackets() {
        String markdown = "$[a,b]$";
        UdmDocument doc = parser.parse(markdown);
        
        System.out.println("=== 测试 $[a,b]$ ===");
        printBlocks(doc);
        
        String formula = extractFormula(doc);
        System.out.println("提取的公式: '" + formula + "'");
        
        assertEquals("[a,b]", formula, "应该正确解析 $[a,b]$ 为公式 [a,b]");
    }

    @Test
    void testFormulaWithExists() {
        String markdown = "$\\exists \\xi \\in (a,b)$";
        UdmDocument doc = parser.parse(markdown);
        
        System.out.println("=== 测试 $\\exists \\xi \\in (a,b)$ ===");
        printBlocks(doc);
        
        String formula = extractFormula(doc);
        System.out.println("提取的公式: '" + formula + "'");
        
        assertEquals("\\exists \\xi \\in (a,b)", formula, "应该正确解析 $\\exists \\xi \\in (a,b)$");
    }

    @Test
    void testFormulaWithDerivative() {
        String markdown = "$f'(\\xi) = 0$";
        UdmDocument doc = parser.parse(markdown);
        
        System.out.println("=== 测试 $f'(\\xi) = 0$ ===");
        printBlocks(doc);
        
        String formula = extractFormula(doc);
        System.out.println("提取的公式: '" + formula + "'");
        
        assertEquals("f'(\\xi) = 0", formula, "应该正确解析 $f'(\\xi) = 0$");
    }

    @Test
    void testComplexDocument() {
        String markdown = "**费马引理：** 若 $f(x)$ 在 $x_0$ 处可导且取得极值，则 $f'(x_0) = 0$\n\n" +
                         "**罗尔定理：** 若 $f(x)$ 在 $[a,b]$ 连续，在 $(a,b)$ 可导，且 $f(a) = f(b)$，则 $\\exists \\xi \\in (a,b)$，使 $f'(\\xi) = 0$";
        
        UdmDocument doc = parser.parse(markdown);
        
        System.out.println("=== 测试复杂文档 ===");
        
        int totalFormulas = 0;
        for (UdmBlock block : doc.getBlocks()) {
            if (block instanceof ParagraphBlock) {
                ParagraphBlock para = (ParagraphBlock) block;
                ParagraphContent content = para.getContent();
                if (content.getSegments() != null) {
                    System.out.println("段落 segments:");
                    for (RichText seg : content.getSegments()) {
                        System.out.println("  text='" + seg.getText() + "', bold=" + seg.getBold() + ", formula='" + seg.getInlineFormula() + "'");
                        if (seg.getInlineFormula() != null) {
                            totalFormulas++;
                        }
                    }
                }
            }
        }
        
        assertTrue(totalFormulas >= 5, "应该正确解析所有公式，实际解析了 " + totalFormulas + " 个");
    }

    private String extractFormula(UdmDocument doc) {
        for (UdmBlock block : doc.getBlocks()) {
            if (block instanceof FormulaBlock) {
                FormulaBlock formulaBlock = (FormulaBlock) block;
                return formulaBlock.getContent().getLatex();
            } else if (block instanceof ParagraphBlock) {
                ParagraphBlock para = (ParagraphBlock) block;
                if (para.getContent().getSegments() != null) {
                    for (RichText seg : para.getContent().getSegments()) {
                        if (seg.getInlineFormula() != null) {
                            return seg.getInlineFormula();
                        }
                    }
                }
            }
        }
        return null;
    }

    private void printBlocks(UdmDocument doc) {
        for (UdmBlock block : doc.getBlocks()) {
            System.out.println("Block type: " + block.getClass().getSimpleName());
            if (block instanceof FormulaBlock) {
                FormulaBlock formulaBlock = (FormulaBlock) block;
                System.out.println("  Formula: " + formulaBlock.getContent().getLatex());
            } else if (block instanceof ParagraphBlock) {
                ParagraphBlock para = (ParagraphBlock) block;
                ParagraphContent content = para.getContent();
                if (content.getSegments() != null) {
                    System.out.println("  Segments count: " + content.getSegments().size());
                    for (RichText seg : content.getSegments()) {
                        System.out.println("    Segment: text='" + seg.getText() + "', formula='" + seg.getInlineFormula() + "'");
                    }
                } else {
                    System.out.println("  Text: " + content.getText());
                }
            }
        }
    }
}
