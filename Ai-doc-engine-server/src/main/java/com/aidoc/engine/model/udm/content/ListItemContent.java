package com.aidoc.engine.model.udm.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 列表项内容
 * 支持富文本和嵌套列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListItemContent {
    
    /**
     * 列表项的富文本内容
     */
    @Builder.Default
    private List<RichText> segments = new ArrayList<>();
    
    /**
     * 嵌套的子列表（可选）
     */
    private ListContent nestedList;
    
    /**
     * 从纯文本创建列表项
     */
    public static ListItemContent of(String text) {
        return ListItemContent.builder()
                .segments(new ArrayList<>(List.of(RichText.of(text))))
                .build();
    }
    
    /**
     * 从富文本列表创建列表项
     */
    public static ListItemContent of(List<RichText> segments) {
        return ListItemContent.builder()
                .segments(segments)
                .build();
    }
}
