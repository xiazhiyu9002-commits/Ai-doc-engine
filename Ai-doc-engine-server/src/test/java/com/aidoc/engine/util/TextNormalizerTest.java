package com.aidoc.engine.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TextNormalizer 测试类
 */
class TextNormalizerTest {

    @Test
    void testCleanExtraSpaces() {
        // 测试连续空格
        assertEquals("这是 一个 测试", TextNormalizer.cleanExtraSpaces("这是  一个   测试"));
        
        // 测试保留换行符
        assertEquals("第一行\n第二行", TextNormalizer.cleanExtraSpaces("第一行\n第二行"));
        
        // 测试清理行首行尾空格
        assertEquals("第一行\n第二行", TextNormalizer.cleanExtraSpaces("  第一行  \n  第二行  "));
        
        // 测试制表符
        assertEquals("测试 制表符", TextNormalizer.cleanExtraSpaces("测试\t\t制表符"));
    }

    @Test
    void testCleanParagraphText() {
        // 测试多余换行符
        assertEquals("段落1\n\n段落2", TextNormalizer.cleanParagraphText("段落1\n\n\n\n段落2"));
        
        // 测试空格和换行符组合
        assertEquals("文本1\n\n文本2", TextNormalizer.cleanParagraphText("文本1  \n\n  文本2"));
    }

    @Test
    void testCleanTableCellText() {
        // 测试移除换行符
        assertEquals("单元格 内容", TextNormalizer.cleanTableCellText("单元格\n内容"));
        
        // 测试多余空格
        assertEquals("单元格 内容", TextNormalizer.cleanTableCellText("单元格   内容"));
        
        // 测试组合
        assertEquals("A B C", TextNormalizer.cleanTableCellText("A  \n  B   \n C"));
    }

    @Test
    void testCleanHeadingText() {
        // 测试移除换行符
        assertEquals("标题 内容", TextNormalizer.cleanHeadingText("标题\n内容"));
        
        // 测试多余空格
        assertEquals("标题 内容", TextNormalizer.cleanHeadingText("标题   内容"));
    }

    @Test
    void testNormalizeWhitespace() {
        // 测试全角空格
        assertEquals("全角 空格", TextNormalizer.normalizeWhitespace("全角\u3000空格"));
        
        // 测试不间断空格
        assertEquals("不间断 空格", TextNormalizer.normalizeWhitespace("不间断\u00A0空格"));
        
        // 测试零宽空格
        assertEquals("零宽空格", TextNormalizer.normalizeWhitespace("零宽\u200B空格"));
        
        // 测试制表符
        assertEquals("制表符 测试", TextNormalizer.normalizeWhitespace("制表符\t测试"));
    }

    @Test
    void testNormalize() {
        // 测试完整流程
        String input = "这是  一个\u3000包含\u00A0多种   空格的\t文本";
        String expected = "这是 一个 包含 多种 空格的 文本";
        assertEquals(expected, TextNormalizer.normalize(input));
    }

    @Test
    void testCleanPreserveFormat() {
        // 测试仅清理首尾空格
        String input = "  代码块  内容  保留  空格  ";
        String expected = "代码块  内容  保留  空格";
        assertEquals(expected, TextNormalizer.cleanPreserveFormat(input));
    }

    @Test
    void testNullAndEmpty() {
        // 测试null
        assertNull(TextNormalizer.cleanExtraSpaces(null));
        assertNull(TextNormalizer.normalize(null));
        
        // 测试空字符串
        assertEquals("", TextNormalizer.cleanExtraSpaces(""));
        assertEquals("", TextNormalizer.normalize(""));
    }

    @Test
    void testChineseText() {
        // 测试中文文本
        assertEquals("这是 中文 测试", TextNormalizer.cleanExtraSpaces("这是  中文   测试"));
        
        // 测试中英文混合
        assertEquals("中文 English 混合", TextNormalizer.cleanExtraSpaces("中文  English   混合"));
    }

    @Test
    void testSpecialCases() {
        // 测试只有空格的字符串
        assertEquals("", TextNormalizer.cleanExtraSpaces("     "));
        
        // 测试只有换行符
        assertEquals("\n", TextNormalizer.cleanExtraSpaces("\n"));
        
        // 测试多个换行符
        assertEquals("\n\n", TextNormalizer.cleanParagraphText("\n\n\n\n"));
    }
}
