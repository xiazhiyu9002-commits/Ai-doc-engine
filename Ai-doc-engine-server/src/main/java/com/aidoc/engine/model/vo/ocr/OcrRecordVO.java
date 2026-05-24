package com.aidoc.engine.model.vo.ocr;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * OCR识别记录视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrRecordVO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 状态：RECOGNIZED, FAILED
     */
    private String status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 处理耗时（毫秒）
     */
    @JsonProperty("processTime")
    private Integer processingTimeMs;
    
    /**
     * 输入内容
     */
    private String inputContent;
    
    /**
     * 输出结果
     */
    @JsonProperty("outputResult")
    private String outputContent;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
