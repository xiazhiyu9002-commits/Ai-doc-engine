package com.aidoc.engine.parser.impl;

import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * 调试 AST 结构
 */
public class AstDebugTest {

    private Parser parser;

    @BeforeEach
    public void setUp() {
        MutableDataSet options = new MutableDataSet()
                .set(Parser.EXTENSIONS, Arrays.asList(
                        TablesExtension.create()
                ))
                .set(Parser.HARD_LINE_BREAK_LIMIT, false);

        this.parser = Parser.builder(options).build();
    }

    @Test
    public void debugPureFormula() {
        String markdown = "$[a,b]$";
        System.out.println("=== 纯公式: " + markdown + " ===");
        
        Node document = parser.parse(markdown);
        printAst(document, 0);
    }

    @Test
    public void debugFormulaWithText() {
        String markdown = "This is text $[a,b]$ and more text";
        System.out.println("=== 公式+文本: " + markdown + " ===");
        
        Node document = parser.parse(markdown);
        printAst(document, 0);
    }

    @Test
    public void debugBoldText() {
        String markdown = "**罗尔定理：**";
        System.out.println("=== 纯加粗: " + markdown + " ===");
        
        Node document = parser.parse(markdown);
        printAst(document, 0);
    }

    @Test
    public void debugBoldWithFormula() {
        String markdown = "**罗尔定理：** 若 $f(x)$ 在闭区间上连续";
        System.out.println("=== 加粗+公式: " + markdown + " ===");
        
        Node document = parser.parse(markdown);
        printAst(document, 0);
    }

    private void printAst(Node node, int level) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indent.append("  ");
        }

        String nodeInfo = node.getClass().getSimpleName();
        String chars = node.getChars().toString().replace("\n", "\\n");
        if (chars.length() > 50) {
            chars = chars.substring(0, 50) + "...";
        }

        System.out.println(indent + nodeInfo + " [" + chars + "]");

        for (Node child : node.getChildren()) {
            printAst(child, level + 1);
        }
    }
}
