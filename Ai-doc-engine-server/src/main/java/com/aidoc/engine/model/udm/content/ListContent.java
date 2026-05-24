package com.aidoc.engine.model.udm.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 列表内容
 * 支持有序/无序列表和嵌套结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListContent {
    
    /**
     * 是否有序列表
     */
    private Boolean ordered;
    
    /**
     * 列表项（简单模式）
     * 仅包含纯文本
     */
    @Builder.Default
    private List<String> items = new ArrayList<>();
    
    /**
     * 列表项内容（高级模式）
     * 支持富文本和嵌套列表
     */
    @Builder.Default
    private List<ListItemContent> itemContents = new ArrayList<>();
    
    /**
     * 创建简单列表
     */
    public static ListContent of(boolean ordered, List<String> items) {
        return ListContent.builder()
                .ordered(ordered)
                .items(items)
                .build();
    }
    
    /**
     * 创建高级列表
     */
    public static ListContent ofItems(boolean ordered, List<ListItemContent> itemContents) {
        return ListContent.builder()
                .ordered(ordered)
                .itemContents(itemContents)
                .build();
    }
    
    /**
     * 是否使用高级模式
     */
    public boolean hasRichItems() {
        return itemContents != null && !itemContents.isEmpty();
    }
}
