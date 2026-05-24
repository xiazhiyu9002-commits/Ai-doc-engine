package com.aidoc.engine.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeedbackStatus {
    
    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    RESOLVED("resolved", "已解决"),
    CLOSED("closed", "已关闭");
    
    private final String code;
    private final String description;
    
    public static FeedbackStatus fromCode(String code) {
        for (FeedbackStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return PENDING;
    }
}
