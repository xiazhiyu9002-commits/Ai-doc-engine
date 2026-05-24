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

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试加粗文本与公式混合解析的问题
 */
public class BoldTextWithFormulaTest {

    private ExtendedMarkdownDocumentParser parser;

    @BeforeEach
    public void setUp() {
        MermaidParserService mermaidParserService = new MermaidParserServiceImpl();
        parser = new ExtendedMarkdownDocumentParser(mermaidParserService);
    }

    @Test
    public void testBoldTextOnly() {
        // 纯加粗文本
        String markdown = "**罗尔定理：**";
        UdmDocument doc = parser.parse(markdown);
        
        List<UdmBlock> blocks = doc.getBlocks();
        assertFalse(blocks.isEmpty(), "Should have at least one block");
        
        ParagraphBlock paraBlock = blocks.stream()
            .filter(b -> b instanceof ParagraphBlock)
            .map(b -> (ParagraphBlock) b)
            .findFirst()
            .orElse(null);
        
        assertNotNull(paraBlock, "Should have a paragraph block");
        
        ParagraphContent content = paraBlock.getContent();
        List<RichText> segments = content.getSegments();
        
        System.out.println("=== 纯加粗文本 ===");
        System.out.println("Segments count: " + segments.size());
        for (RichText seg : segments) {
            System.out.println("  Segment: text='" + seg.getText() + "', bold=" + seg.getBold() + ", formula='" + seg.getInlineFormula() + "'");
        }
        
        // 应该有一个加粗的片段
        boolean hasBoldText = segments.stream()
            .anyMatch(s -> Boolean.TRUE.equals(s.getBold()) && s.getText().contains("罗尔定理"));
        
        assertTrue(hasBoldText, "Should have bold text '罗尔定理'");
        
        // 不应该包含星号
        boolean containsAsterisks = segments.stream()
            .anyMatch(s -> s.getText() != null && s.getText().contains("**"));
        assertFalse(containsAsterisks, "Should not contain ** in text");
    }

    @Test
    public void testBoldTextWithFormula() {
        // 加粗文本 + 公式
        String markdown = "**罗尔定理：** 若 $f(x)$ 在闭区间上连续";
        UdmDocument doc = parser.parse(markdown);
        
        List<UdmBlock> blocks = doc.getBlocks();
        assertFalse(blocks.isEmpty(), "Should have at least one block");
        
        ParagraphBlock paraBlock = blocks.stream()
            .filter(b -> b instanceof ParagraphBlock)
            .map(b -> (ParagraphBlock) b)
            .findFirst()
            .orElse(null);
        
        assertNotNull(paraBlock, "Should have a paragraph block");
        
        ParagraphContent content = paraBlock.getContent();
        List<RichText> segments = content.getSegments();
        
        System.out.println("=== 加粗文本 + 公式 ===");
        System.out.println("Segments count: " + segments.size());
        for (RichText seg : segments) {
            System.out.println("  Segment: text='" + seg.getText() + "', bold=" + seg.getBold() + ", formula='" + seg.getInlineFormula() + "'");
        }
        
        // 应该有一个加粗的片段
        boolean hasBoldText = segments.stream()
            .anyMatch(s -> Boolean.TRUE.equals(s.getBold()) && s.getText() != null && s.getText().contains("罗尔定理"));
        
        assertTrue(hasBoldText, "Should have bold text '罗尔定理'");
        
        // 应该有公式
        boolean hasFormula = segments.stream()
            .anyMatch(s -> s.getInlineFormula() != null && s.getInlineFormula().contains("f(x)"));
        
        assertTrue(hasFormula, "Should have inline formula f(x)");
        
        // 不应该包含星号
        boolean containsAsterisks = segments.stream()
            .anyMatch(s -> s.getText() != null && s.getText().contains("**"));
        assertFalse(containsAsterisks, "Should not contain ** in text");
    }

    @Test
    public void testFormulaWithBoldText() {
        // 公式 + 加粗文本
        String markdown = "设 $f(x)$ 满足条件，**则结论成立**";
        UdmDocument doc = parser.parse(markdown);
        
        List<UdmBlock> blocks = doc.getBlocks();
        assertFalse(blocks.isEmpty(), "Should have at least one block");
        
        ParagraphBlock paraBlock = blocks.stream()
            .filter(b -> b instanceof ParagraphBlock)
            .map(b -> (ParagraphBlock) b)
            .findFirst()
            .orElse(null);
        
        assertNotNull(paraBlock, "Should have a paragraph block");
        
        ParagraphContent content = paraBlock.getContent();
        List<RichText> segments = content.getSegments();
        
        System.out.println("=== 公式 + 加粗文本 ===");
        System.out.println("Segments count: " + segments.size());
        for (RichText seg : segments) {
            System.out.println("  Segment: text='" + seg.getText() + "', bold=" + seg.getBold() + ", formula='" + seg.getInlineFormula() + "'");
        }
        
        // 应该有公式
        boolean hasFormula = segments.stream()
            .anyMatch(s -> s.getInlineFormula() != null && s.getInlineFormula().contains("f(x)"));
        
        assertTrue(hasFormula, "Should have inline formula f(x)");
        
        // 应该有一个加粗的片段
        boolean hasBoldText = segments.stream()
            .anyMatch(s -> Boolean.TRUE.equals(s.getBold()) && s.getText() != null && s.getText().contains("结论成立"));
        
        assertTrue(hasBoldText, "Should have bold text '结论成立'");
    }

    @Test
    public void testMultipleFormulasAndBold() {
        // 多个公式和加粗文本混合
        String markdown = "**定理：** 若 $f(x)$ 和 $g(x)$ 都连续，**则** $h(x)$ 也连续";
        UdmDocument doc = parser.parse(markdown);
        
        List<UdmBlock> blocks = doc.getBlocks();
        assertFalse(blocks.isEmpty(), "Should have at least one block");
        
        ParagraphBlock paraBlock = blocks.stream()
            .filter(b -> b instanceof ParagraphBlock)
            .map(b -> (ParagraphBlock) b)
            .findFirst()
            .orElse(null);
        
        assertNotNull(paraBlock, "Should have a paragraph block");
        
        ParagraphContent content = paraBlock.getContent();
        List<RichText> segments = content.getSegments();
        
        System.out.println("=== 多个公式和加粗文本混合 ===");
        System.out.println("Segments count: " + segments.size());
        for (RichText seg : segments) {
            System.out.println("  Segment: text='" + seg.getText() + "', bold=" + seg.getBold() + ", formula='" + seg.getInlineFormula() + "'");
        }
        
        // 应该有3个公式
        long formulaCount = segments.stream()
            .filter(s -> s.getInlineFormula() != null)
            .count();
        assertEquals(3, formulaCount, "Should have 3 formulas");
        
        // 应该有2个加粗片段
        long boldCount = segments.stream()
            .filter(s -> Boolean.TRUE.equals(s.getBold()))
            .count();
        assertTrue(boldCount >= 2, "Should have at least 2 bold segments");
    }
}
