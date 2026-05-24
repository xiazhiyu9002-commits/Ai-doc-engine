package com.aidoc.engine.model.udm.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 富文本段
 * 表示段落中的富文本元素，支持加粗、斜体等格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RichText {
    
    /**
     * 文本内容
     */
    private String text;
    
    /**
     * 是否加粗
     */
    @Builder.Default
    private Boolean bold = false;
    
    /**
     * 是否斜体
     */
    @Builder.Default
    private Boolean italic = false;
    
    /**
     * 是否删除线
     */
    @Builder.Default
    private Boolean strikethrough = false;
    
    /**
     * 是否行内代码
     */
    @Builder.Default
    private Boolean code = false;
    
    /**
     * 链接地址（可选）
     */
    private String linkUrl;
    
    /**
     * 行内公式（可选）
     */
    private String inlineFormula;
    
    /**
     * 创建普通文本
     */
    public static RichText of(String text) {
        return RichText.builder().text(text).build();
    }
    
    /**
     * 创建加粗文本
     */
    public static RichText bold(String text) {
        return RichText.builder().text(text).bold(true).build();
    }
    
    /**
     * 创建斜体文本
     */
    public static RichText italic(String text) {
        return RichText.builder().text(text).italic(true).build();
    }
    
    /**
     * 创建行内代码
     */
    public static RichText code(String text) {
        return RichText.builder().text(text).code(true).build();
    }
    
    /**
     * 创建链接
     */
    public static RichText link(String text, String url) {
        return RichText.builder().text(text).linkUrl(url).build();
    }
    
    /**
     * 创建行内公式
     */
    public static RichText formula(String latex) {
        return RichText.builder().inlineFormula(latex).build();
    }
}
