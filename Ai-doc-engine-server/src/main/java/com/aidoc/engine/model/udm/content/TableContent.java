package com.aidoc.engine.model.udm.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 表格内容
 * 支持纯文本和富文本单元格
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableContent {
    
    /**
     * 表头（简单模式）
     */
    @Builder.Default
    private List<String> headers = new ArrayList<>();
    
    /**
     * 表格行（简单模式）
     */
    @Builder.Default
    private List<List<String>> rows = new ArrayList<>();
    
    /**
     * 表头单元格（高级模式）
     */
    @Builder.Default
    private List<List<RichText>> headerCells = new ArrayList<>();
    
    /**
     * 数据行单元格（高级模式）
     */
    @Builder.Default
    private List<List<List<RichText>>> rowCells = new ArrayList<>();
    
    /**
     * 创建简单表格
     */
    public static TableContent of(List<String> headers, List<List<String>> rows) {
        return TableContent.builder()
                .headers(headers)
                .rows(rows)
                .build();
    }
    
    /**
     * 是否使用高级模式
     */
    public boolean hasRichCells() {
        return headerCells != null && !headerCells.isEmpty();
    }
}
