package com.aidoc.engine.model.udm.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程图内容
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowchartContent {
    
    /**
     * 原始 Mermaid 源码
     * 用于渲染图片
     */
    private String rawSource;
    
    /**
     * 源码类型（固定为 mermaid）
     */
    private String sourceType;
    
    /**
     * 渲染后的图片（base64 编码）
     * 用于前端预览显示或直接嵌入 Word
     */
    private String imageBase64;
}
