package com.aidoc.engine.model.dto.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateConfig {
    
    private PageSettings pageSettings;
    private FontSettings fontSettings;
    private ParagraphSettings paragraphSettings;
    private HeaderFooterSettings headerFooterSettings;
    
    /**
     * 页面设置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageSettings {
        private String pageSize;        // A4, A3, Letter等
        private String orientation;     // portrait(纵向), landscape(横向)
        private Margins margins;
    }
    
    /**
     * 页边距
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Margins {
        private Double top;
        private Double bottom;
        private Double left;
        private Double right;
        private Double gutter;
    }
    
    /**
     * 字体设置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FontSettings {
        private String fontFamily;
        private Integer fontSize;
        private HeadingFonts headingFonts;
        private String codeFontFamily;
        private Integer codeFontSize;
    }
    
    /**
     * 标题字体
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeadingFonts {
        private HeadingFont h1;
        private HeadingFont h2;
        private HeadingFont h3;
        private HeadingFont h4;
        private HeadingFont h5;
        private HeadingFont h6;
    }
    
    /**
     * 单个标题字体配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeadingFont {
        private String family;
        private Integer size;
        private Boolean bold;
    }
    
    /**
     * 段落设置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParagraphSettings {
        private Double lineSpacing;
        private ParagraphSpacing paragraphSpacing;
        private Integer firstLineIndent;
        private String alignment;       // left, center, right, justify
    }
    
    /**
     * 段落间距
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParagraphSpacing {
        private Integer before;
        private Integer after;
    }
    
    /**
     * 页眉页脚设置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeaderFooterSettings {
        private String header;
        private String footer;
        private Boolean firstPageDifferent;
        private Boolean oddEvenDifferent;
        private Double headerHeight;
        private Double footerHeight;
    }
}
