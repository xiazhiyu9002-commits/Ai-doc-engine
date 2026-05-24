package com.aidoc.engine.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 导出任务状态枚举
 */
@Getter
@AllArgsConstructor
public enum ExportTaskStatus {
    
    PENDING("PENDING", "待处理"),
    PROCESSING("PROCESSING", "处理中"),
    SUCCESS("SUCCESS", "成功"),
    FAILED("FAILED", "失败");
    
    private final String code;
    private final String description;
    
    public static ExportTaskStatus fromCode(String code) {
        for (ExportTaskStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
