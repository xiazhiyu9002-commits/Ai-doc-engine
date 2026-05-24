package com.aidoc.engine.parser.impl;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RegexDebugTest {

    @Test
    void testFormulaRegex() {
        String regex = "(\\$\\$(.+?)\\$\\$)|((?<!\\\\)\\$([^$\\n]+?)(?<!\\\\)\\$)|(\\\\\\((.+?)\\\\\\))|(\\\\\\[(.+?)\\\\\\])";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        
        String[] testCases = {
            "$[a,b]$",
            "$\\exists \\xi \\in (a,b)$",
            "$f'(\\xi) = 0$",
            "$f(x)$",
            "$x_0$"
        };
        
        System.out.println("正则表达式: " + regex);
        System.out.println();
        
        for (String test : testCases) {
            System.out.println("=== 测试: " + test + " ===");
            Matcher matcher = pattern.matcher(test);
            boolean found = false;
            while (matcher.find()) {
                found = true;
                System.out.println("  匹配成功: '" + matcher.group(0) + "'");
                System.out.println("  group(4): '" + matcher.group(4) + "'");
            }
            if (!found) {
                System.out.println("  匹配失败!");
            }
            System.out.println();
        }
    }
}
