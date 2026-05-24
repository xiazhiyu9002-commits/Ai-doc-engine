package com.aidoc.engine.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeedbackType {
    
    SUGGESTION("suggestion", "建议"),
    BUG("bug", "问题反馈"),
    FEATURE("feature", "功能请求"),
    OTHER("other", "其他");
    
    private final String code;
    private final String description;
    
    public static FeedbackType fromCode(String code) {
        for (FeedbackType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return OTHER;
    }
}
