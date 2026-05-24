package com.aidoc.engine.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 文本规范化工具类
 * 用于清理和规范化文本内容，确保导出格式整洁
 */
@Slf4j
public class TextNormalizer {
    
    /**
     * 清理文本中的多余空格
     * - 将连续的空格替换为单个空格
     * - 保留换行符
     * - 清理首尾空格
     * 
     * @param text 原始文本
     * @return 清理后的文本
     */
    public static String cleanExtraSpaces(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 将连续的空格（不包括换行符）替换为单个空格
        // 使用 [ \t]+ 匹配空格和制表符，但不匹配换行符
        String result = text.replaceAll("[ \\t]+", " ");
        
        // 清理行首和行尾的空格，但保留换行符
        result = result.replaceAll(" *\\n *", "\n");
        
        // 清理整个字符串的首尾空格
        result = result.trim();
        
        return result;
    }
    
    /**
     * 清理段落文本
     * - 清理多余空格
     * - 移除多余的换行符（连续超过2个换行符）
     * - 保留段落内的单个换行符
     * 
     * @param text 原始段落文本
     * @return 清理后的段落文本
     */
    public static String cleanParagraphText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 先清理多余空格
        String result = cleanExtraSpaces(text);
        
        // 将连续的多个换行符（超过2个）替换为2个换行符
        result = result.replaceAll("\\n{3,}", "\n\n");
        
        return result;
    }
    
    /**
     * 清理表格单元格文本
     * - 清理多余空格
     * - 移除所有换行符（表格单元格通常不需要换行）
     * - 清理首尾空格
     * 
     * @param text 原始单元格文本
     * @return 清理后的单元格文本
     */
    public static String cleanTableCellText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 将换行符替换为空格
        String result = text.replace("\n", " ");
        
        // 清理多余空格
        result = cleanExtraSpaces(result);
        
        return result;
    }
    
    /**
     * 清理标题文本
     * - 移除 Markdown 标题标记 (如 ###)
     * - 清理多余空格
     * - 移除所有换行符
     * - 清理首尾空格
     * 
     * @param text 原始标题文本
     * @return 清理后的标题文本
     */
    public static String cleanHeadingText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 移除开头的 Markdown 标题标记 (如 #, ##, ### 等)
        String result = text.replaceAll("^#+\\s*", "");
        
        // 移除换行符
        result = result.replace("\n", " ");
        
        // 清理多余空格
        result = cleanExtraSpaces(result);
        
        return result;
    }
    
    /**
     * 规范化空白字符
     * - 将各种空白字符（全角空格、不间断空格等）统一为普通空格
     * - 保留换行符
     * 
     * @param text 原始文本
     * @return 规范化后的文本
     */
    public static String normalizeWhitespace(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 将全角空格替换为半角空格
        String result = text.replace('\u3000', ' ');
        
        // 将不间断空格替换为普通空格
        result = result.replace('\u00A0', ' ');
        
        // 将零宽空格移除（使用字符串替换而不是字符替换）
        result = result.replace("\u200B", "");
        
        // 将制表符替换为空格
        result = result.replace('\t', ' ');
        
        return result;
    }
    
    /**
     * 完整的文本清理流程
     * - 规范化空白字符
     * - 清理多余空格
     * - 清理首尾空格
     * 
     * @param text 原始文本
     * @return 清理后的文本
     */
    public static String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 1. 规范化空白字符
        String result = normalizeWhitespace(text);
        
        // 2. 清理多余空格
        result = cleanExtraSpaces(result);
        
        return result;
    }
    
    /**
     * 保留原始格式的文本清理
     * 仅清理首尾空格，保留内部所有空格和换行符
     * 用于代码块等需要保留原始格式的场景
     * 
     * @param text 原始文本
     * @return 清理后的文本
     */
    public static String cleanPreserveFormat(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 仅清理首尾空格
        return text.trim();
    }
}
