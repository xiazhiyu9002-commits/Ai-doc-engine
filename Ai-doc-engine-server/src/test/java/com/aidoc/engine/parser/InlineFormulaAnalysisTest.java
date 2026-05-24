package com.aidoc.engine.parser;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.ast.Node;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test to analyze inline formula parsing issues
 */
public class InlineFormulaAnalysisTest {

    @Test
    public void testFlexmarkParsing() {
        MutableDataSet options = new MutableDataSet()
                .set(Parser.EXTENSIONS, Arrays.asList(
                        TablesExtension.create()
                ))
                .set(Parser.HARD_LINE_BREAK_LIMIT, false);

        Parser parser = Parser.builder(options).build();

        String[] testCases = {
            "$[a,b]$",
            "$\\exists \\xi \\in (a,b)$",
            "$f'(\\xi) = 0$",
            "Text $[a,b]$ text",
            "Text $\\exists \\xi \\in (a,b)$ text",
            "Text $f'(\\xi) = 0$ text"
        };

        for (String testCase : testCases) {
            System.out.println("\n========== Test: \"" + testCase + "\" ==========");
            Node document = parser.parse(testCase);
            System.out.println("Root node type: " + document.getClass().getSimpleName());

            for (Node child : document.getChildren()) {
                printNode(child, 1);
            }
        }
    }

    @Test
    public void testRegexMatching() {
        Pattern pattern = Pattern.compile(
            "(\\$\\$(.+?)\\$\\$)|((?<!\\\\)\\$([^$\\n]+?)(?<!\\\\)\\$)|(\\\\\\((.+?)\\\\\\))|(\\\\\\[(.+?)\\\\\\])",
            Pattern.DOTALL
        );

        String[] testCases = {
            "$[a,b]$",
            "$\\exists \\xi \\in (a,b)$",
            "$f'(\\xi) = 0$",
            " $[a,b]$",
            "Text $[a,b]$ text",
            "Text $\\exists \\xi \\in (a,b)$ text",
            "Text $f'(\\xi) = 0$ text"
        };

        for (String testCase : testCases) {
            System.out.println("\n========== Test: \"" + testCase + "\" ==========");
            Matcher matcher = pattern.matcher(testCase);

            int matchCount = 0;
            while (matcher.find()) {
                matchCount++;
                System.out.println("Match " + matchCount + ":");
                System.out.println("  Full match: \"" + matcher.group(0) + "\"");
                System.out.println("  Start: " + matcher.start());
                System.out.println("  End: " + matcher.end());

                if (matcher.group(2) != null) {
                    System.out.println("  Type: Block formula ($$)");
                    System.out.println("  Content: \"" + matcher.group(2) + "\"");
                } else if (matcher.group(4) != null) {
                    System.out.println("  Type: Inline formula ($)");
                    System.out.println("  Content: \"" + matcher.group(4) + "\"");
                } else if (matcher.group(6) != null) {
                    System.out.println("  Type: Inline formula (\\(\\))");
                    System.out.println("  Content: \"" + matcher.group(6) + "\"");
                } else if (matcher.group(8) != null) {
                    System.out.println("  Type: Block formula (\\[\\])");
                    System.out.println("  Content: \"" + matcher.group(8) + "\"");
                }
            }

            if (matchCount == 0) {
                System.out.println("No match found");
            }
        }
    }

    private void printNode(Node node, int indent) {
        String prefix = "  ".repeat(indent);
        System.out.println(prefix + "Node: " + node.getClass().getSimpleName());

        if (node instanceof Text) {
            Text textNode = (Text) node;
            System.out.println(prefix + "  Text content: \"" + textNode.getChars() + "\"");
        } else if (node instanceof Paragraph) {
            Paragraph para = (Paragraph) node;
            System.out.println(prefix + "  Paragraph content: \"" + para.getContentChars() + "\"");
        }

        for (Node child : node.getChildren()) {
            printNode(child, indent + 1);
        }
    }
}
