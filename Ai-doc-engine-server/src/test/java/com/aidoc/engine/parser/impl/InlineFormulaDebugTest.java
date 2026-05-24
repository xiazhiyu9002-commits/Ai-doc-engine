package com.aidoc.engine.parser.impl;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.ast.Node;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InlineFormulaDebugTest {

    @Test
    public void testFlexmarkNodeStructure() {
        MutableDataSet options = new MutableDataSet()
                .set(Parser.EXTENSIONS, Arrays.asList(
                        TablesExtension.create()
                ))
                .set(Parser.HARD_LINE_BREAK_LIMIT, false);

        Parser parser = Parser.builder(options).build();

        String[] testCases = {
            "$[a,b]$",
            "$\\exists \\xi \\in (a,b)$",
            "$f'(\\xi) = 0$"
        };

        for (String testCase : testCases) {
            System.out.println("\n========================================");
            System.out.println("Input: \"" + testCase + "\"");
            System.out.println("========================================");

            Node document = parser.parse(testCase);

            for (Node child : document.getChildren()) {
                if (child instanceof Paragraph) {
                    Paragraph para = (Paragraph) child;
                    System.out.println("Paragraph found");
                    System.out.println("  Full content: \"" + para.getContentChars() + "\"");
                    
                    int childCount = 0;
                    for (Node n : para.getChildren()) {
                        childCount++;
                    }
                    System.out.println("  Children count: " + childCount);

                    int i = 0;
                    for (Node paraChild : para.getChildren()) {
                        i++;
                        System.out.println("  Child " + i + ": " + paraChild.getClass().getSimpleName());
                        if (paraChild instanceof Text) {
                            Text textNode = (Text) paraChild;
                            System.out.println("    Text: \"" + textNode.getChars() + "\"");
                        }
                    }

                    // Test regex on full paragraph content
                    System.out.println("\n  Testing regex on full content:");
                    testRegexOnText(para.getContentChars().toString());

                    // Test regex on each child node
                    System.out.println("\n  Testing regex on each child node:");
                    for (Node paraChild : para.getChildren()) {
                        if (paraChild instanceof Text) {
                            Text textNode = (Text) paraChild;
                            String text = textNode.getChars().toString();
                            System.out.println("    Node text: \"" + text + "\"");
                            testRegexOnText(text);
                        }
                    }
                }
            }
        }
    }

    private void testRegexOnText(String text) {
        Pattern pattern = Pattern.compile(
            "(\\$\\$(.+?)\\$\\$)|((?<!\\\\)\\$([^$\\n]+?)(?<!\\\\)\\$)|(\\\\\\((.+?)\\\\\\))|(\\\\\\[(.+?)\\\\\\])",
            Pattern.DOTALL
        );

        Matcher matcher = pattern.matcher(text);
        boolean found = false;
        while (matcher.find()) {
            found = true;
            System.out.println("      Match found: \"" + matcher.group(0) + "\"");
            if (matcher.group(4) != null) {
                System.out.println("      Formula content: \"" + matcher.group(4) + "\"");
            }
        }
        if (!found) {
            System.out.println("      No match found");
        }
    }
}
