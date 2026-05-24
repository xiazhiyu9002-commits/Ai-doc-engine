package com.aidoc.engine.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeedbackPriority {
    
    LOW("low", "低"),
    NORMAL("normal", "普通"),
    HIGH("high", "高"),
    URGENT("urgent", "紧急");
    
    private final String code;
    private final String description;
    
    public static FeedbackPriority fromCode(String code) {
        for (FeedbackPriority priority : values()) {
            if (priority.getCode().equalsIgnoreCase(code)) {
                return priority;
            }
        }
        return NORMAL;
    }
}
