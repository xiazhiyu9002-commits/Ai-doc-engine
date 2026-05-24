package com.aidoc.engine.model.udm.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 段落内容
 * 支持纯文本和富文本两种模式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParagraphContent {
    
    /**
     * 段落文本（简单模式）
     * 当 segments 为空时使用
     */
    private String text;
    
    /**
     * 富文本段列表（高级模式）
     * 支持加粗、斜体、链接等格式
     */
    @Builder.Default
    private List<RichText> segments = new ArrayList<>();
    
    /**
     * 创建纯文本段落
     */
    public static ParagraphContent of(String text) {
        return ParagraphContent.builder()
                .text(text)
                .build();
    }
    
    /**
     * 创建富文本段落
     */
    public static ParagraphContent ofRichText(List<RichText> segments) {
        return ParagraphContent.builder()
                .segments(segments)
                .build();
    }
    
    /**
     * 获取有效内容
     * 优先返回富文本段，否则返回纯文本
     */
    @JsonIgnore
    public List<RichText> getEffectiveSegments() {
        if (segments != null && !segments.isEmpty()) {
            return segments;
        }
        if (text != null && !text.isEmpty()) {
            return List.of(RichText.of(text));
        }
        return new ArrayList<>();
    }
    
    /**
     * 是否为富文本模式
     */
    public boolean isRichText() {
        return segments != null && !segments.isEmpty();
    }
}
