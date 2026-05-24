package com.aidoc.engine.parser.impl;

import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.UdmDocument;
import com.aidoc.engine.model.udm.block.ParagraphBlock;
import com.aidoc.engine.model.udm.content.ParagraphContent;
import com.aidoc.engine.model.udm.content.RichText;
import com.aidoc.engine.service.MermaidParserService;
import com.aidoc.engine.service.impl.MermaidParserServiceImpl;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FullTextDebugTest {

    private ExtendedMarkdownDocumentParser parser;

    @BeforeEach
    void setUp() {
        MermaidParserService mermaidParserService = new MermaidParserServiceImpl();
        parser = new ExtendedMarkdownDocumentParser(mermaidParserService);
    }

    @Test
    void debugFullText() {
        String markdown = "**罗尔定理：** 若 $f(x)$ 在 $[a,b]$ 连续";
        
        System.out.println("=== 原始 Markdown ===");
        System.out.println(markdown);
        System.out.println();
        
        MutableDataSet options = new MutableDataSet()
                .set(Parser.EXTENSIONS, Arrays.asList(com.vladsch.flexmark.ext.tables.TablesExtension.create()))
                .set(Parser.HARD_LINE_BREAK_LIMIT, false);
        Parser flexmarkParser = Parser.builder(options).build();
        Node document = flexmarkParser.parse(markdown);
        
        for (Node node : document.getChildren()) {
            if (node instanceof Paragraph) {
                Paragraph para = (Paragraph) node;
                String fullText = para.getChars().toString();
                
                System.out.println("=== Paragraph fullText ===");
                System.out.println("Length: " + fullText.length());
                System.out.println("Content: '" + fullText + "'");
                System.out.println();
                
                System.out.println("=== 子节点 ===");
                for (Node child : para.getChildren()) {
                    System.out.println("  " + child.getClass().getSimpleName() + ": '" + child.getChars() + "'");
                }
                System.out.println();
                
                System.out.println("=== 正则匹配测试 ===");
                Pattern pattern = Pattern.compile(
                    "(\\$\\$(.+?)\\$\\$)|((?<!\\\\)\\$([^$\\n]+?)(?<!\\\\)\\$)|(\\\\\\((.+?)\\\\\\))|(\\\\\\[(.+?)\\\\\\])",
                    Pattern.DOTALL
                );
                Matcher matcher = pattern.matcher(fullText);
                while (matcher.find()) {
                    System.out.println("  匹配: '" + matcher.group(0) + "'");
                    if (matcher.group(4) != null) {
                        System.out.println("    公式内容: '" + matcher.group(4) + "'");
                    }
                }
            }
        }
    }
}
