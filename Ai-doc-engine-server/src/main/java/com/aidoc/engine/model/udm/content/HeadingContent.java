package com.aidoc.engine.model.udm.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标题内容
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeadingContent {
    
    /**
     * 标题级别（1-6）
     */
    private Integer level;
    
    /**
     * 标题文本
     */
    private String text;
}
