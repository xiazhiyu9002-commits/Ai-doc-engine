package com.aidoc.engine.util;

import lombok.extern.slf4j.Slf4j;

/**
 * OMML 清理工具类
 * 用于清理 MathJax 生成的不符合 Word OMML 规范的元素
 */
@Slf4j
public class OmmlCleaner {
    
    /**
     * 清理不符合 Word OMML 规范的元素
     * MathJax 生成的 OMML 可能包含一些 Word 不支持的元素或元素顺序
     * 
     * @param omml 原始 OMML 字符串
     * @return 清理后的 OMML 字符串
     */
    public static String clean(String omml) {
        if (omml == null || omml.isEmpty()) {
            return omml;
        }
        
        String result = omml;
        
        // 移除 scrLvl 元素（script level）
        // 这个元素在某些位置不被 Word 支持，会导致解析错误
        result = result.replaceAll("<m:scrLvl\\s+m:val=\"[^\"]*\"\\s*/>", "");
        result = result.replaceAll("<m:scrLvl>.*?</m:scrLvl>", "");
        
        // 移除 argSz 元素（argument size）
        // 某些情况下也会导致问题
        result = result.replaceAll("<m:argSz\\s+m:val=\"[^\"]*\"\\s*/>", "");
        result = result.replaceAll("<m:argSz>.*?</m:argSz>", "");
        
        // 移除 baseJc 元素（baseline justification）
        result = result.replaceAll("<m:baseJc\\s+m:val=\"[^\"]*\"\\s*/>", "");
        result = result.replaceAll("<m:baseJc>.*?</m:baseJc>", "");
        
        // 调整大型运算符的显示模式
        // 将 limLoc="undOvr" 改为 limLoc="subSup"，使符号更紧凑
        result = adjustLargeOperators(result);
        
        // 移除空的分隔符（括号）元素
        result = removeEmptyDelimiters(result);
        
        if (log.isDebugEnabled() && !omml.equals(result)) {
            log.debug("清理 OMML: 长度 {} -> {}", omml.length(), result.length());
        }
        
        return result;
    }
    
    /**
     * 调整大型运算符的显示模式
     * 将 limLoc="undOvr" 改为 limLoc="subSup"，使符号更紧凑
     */
    private static String adjustLargeOperators(String omml) {
        if (omml == null || omml.isEmpty()) {
            return omml;
        }
        
        // 将 limLoc="undOvr" 改为 limLoc="subSup"
        // 这会让 ∏、∑ 等大型运算符的上下标显示在右侧而不是上下方
        String result = omml.replaceAll(
            "<m:limLoc\\s+m:val=\"undOvr\"\\s*/>",
            "<m:limLoc m:val=\"subSup\"/>"
        );
        
        if (!result.equals(omml) && log.isDebugEnabled()) {
            log.debug("调整大型运算符显示模式: undOvr -> subSup");
        }
        
        return result;
    }
    
    /**
     * 移除空的分隔符（括号）元素
     */
    private static String removeEmptyDelimiters(String omml) {
        if (omml == null || omml.isEmpty()) {
            return omml;
        }
        
        String result = omml;
        
        // 多次迭代，直到没有变化为止（最多10次）
        for (int iteration = 0; iteration < 10; iteration++) {
            String before = result;
            
            // 移除空的 <m:d> 元素（括号）
            // 模式1: <m:d><m:dPr>...</m:dPr><m:e/></m:d>
            result = result.replaceAll("<m:d>\\s*<m:dPr[^>]*>.*?</m:dPr>\\s*<m:e\\s*/?>\\s*</m:d>", "");
            result = result.replaceAll("<m:d>\\s*<m:dPr[^>]*/>\\s*<m:e\\s*/?>\\s*</m:d>", "");
            
            // 模式2: <m:d><m:e/></m:d> 或 <m:d><m:e></m:e></m:d>
            result = result.replaceAll("<m:d>\\s*<m:e\\s*/>\\s*</m:d>", "");
            result = result.replaceAll("<m:d>\\s*<m:e>\\s*</m:e>\\s*</m:d>", "");
            
            // 模式3: <m:d><m:dPr.../><m:e>空内容</m:e></m:d>
            result = result.replaceAll("<m:d>\\s*<m:dPr[^>]*/>\\s*<m:e>\\s*</m:e>\\s*</m:d>", "");
            result = result.replaceAll("<m:d>\\s*<m:dPr[^>]*>.*?</m:dPr>\\s*<m:e>\\s*</m:e>\\s*</m:d>", "");
            
            // 模式4: 完全空的 <m:d>
            result = result.replaceAll("<m:d\\s*/>", "");
            result = result.replaceAll("<m:d>\\s*</m:d>", "");
            
            // 清理产生的空元素
            result = result.replaceAll("<m:e>\\s*</m:e>", "");
            result = result.replaceAll("<m:e\\s*/>", "");
            result = result.replaceAll("<m:r>\\s*</m:r>", "");
            result = result.replaceAll("<m:t>\\s*</m:t>", "");
            result = result.replaceAll("<m:t\\s*/>", "");
            
            // 如果没有变化，说明清理完成
            if (before.equals(result)) {
                if (log.isTraceEnabled()) {
                    log.trace("清理空分隔符完成，迭代次数: {}", iteration + 1);
                }
                break;
            }
        }
        
        return result;
    }
}
