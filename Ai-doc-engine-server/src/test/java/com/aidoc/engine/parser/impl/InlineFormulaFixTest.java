package com.aidoc.engine.parser.impl;

import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.UdmDocument;
import com.aidoc.engine.model.udm.block.FormulaBlock;
import com.aidoc.engine.model.udm.block.ParagraphBlock;
import com.aidoc.engine.model.udm.content.FormulaContent;
import com.aidoc.engine.model.udm.content.ParagraphContent;
import com.aidoc.engine.model.udm.content.RichText;
import com.aidoc.engine.service.MermaidParserService;
import com.aidoc.engine.service.impl.MermaidParserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InlineFormulaFixTest {

    private ExtendedMarkdownDocumentParser parser;

    @BeforeEach
    public void setUp() {
        MermaidParserService mermaidParserService = new MermaidParserServiceImpl();
        parser = new ExtendedMarkdownDocumentParser(mermaidParserService);
    }

    @Test
    public void testInlineFormulaWithBrackets() {
        String markdown = "$[a,b]$";
        UdmDocument doc = parser.parse(markdown);
        
        List<UdmBlock> blocks = doc.getBlocks();
        assertFalse(blocks.isEmpty(), "Should have at least one block");
        
        // When paragraph contains only one formula, it's converted to FormulaBlock
        FormulaBlock formulaBlock = blocks.stream()
            .filter(b -> b instanceof FormulaBlock)
            .map(b -> (FormulaBlock) b)
            .findFirst()
            .orElse(null);
        
        assertNotNull(formulaBlock, "Should have a formula block");
        
        FormulaContent content = formulaBlock.getContent();
        assertNotNull(content, "Formula should have content");
        assertEquals("[a,b]", content.getLatex(), "Formula should be [a,b]");
    }

    @Test
    public void testInlineFormulaWithExists() {
        String markdown = "$\\exists \\xi \\in (a,b)$";
        UdmDocument doc = parser.parse(markdown);
        
        List<UdmBlock> blocks = doc.getBlocks();
        assertFalse(blocks.isEmpty(), "Should have at least one block");
        
        FormulaBlock formulaBlock = blocks.stream()
            .filter(b -> b instanceof FormulaBlock)
            .map(b -> (FormulaBlock) b)
            .findFirst()
            .orElse(null);
        
        assertNotNull(formulaBlock, "Should have a formula block");
        
        FormulaContent content = formulaBlock.getContent();
        assertNotNull(content, "Formula should have content");
        assertEquals("\\exists \\xi \\in (a,b)", content.getLatex(), "Formula content should match");
    }

    @Test
    public void testInlineFormulaWithDerivative() {
        String markdown = "$f'(\\xi) = 0$";
        UdmDocument doc = parser.parse(markdown);
        
        List<UdmBlock> blocks = doc.getBlocks();
        assertFalse(blocks.isEmpty(), "Should have at least one block");
        
        FormulaBlock formulaBlock = blocks.stream()
            .filter(b -> b instanceof FormulaBlock)
            .map(b -> (FormulaBlock) b)
            .findFirst()
            .orElse(null);
        
        assertNotNull(formulaBlock, "Should have a formula block");
        
        FormulaContent content = formulaBlock.getContent();
        assertNotNull(content, "Formula should have content");
        assertEquals("f'(\\xi) = 0", content.getLatex(), "Formula content should match");
    }

    @Test
    public void testInlineFormulaWithSurroundingText() {
        String markdown = "This is text $[a,b]$ and more text";
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
        
        System.out.println("Segments count: " + segments.size());
        for (RichText seg : segments) {
            System.out.println("  Segment: text='" + seg.getText() + "', formula='" + seg.getInlineFormula() + "'");
        }
        
        // Should have 3 segments: text before, formula, text after
        assertTrue(segments.size() >= 2, "Should have at least 2 segments");
        
        boolean hasFormula = segments.stream()
            .anyMatch(s -> s.getInlineFormula() != null && s.getInlineFormula().equals("[a,b]"));
        
        assertTrue(hasFormula, "Should have inline formula [a,b]");
    }
}
