package com.aidoc.engine.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeedbackOperationType {
    
    CREATE("create", "创建"),
    UPDATE_STATUS("update_status", "更新状态"),
    UPDATE_PRIORITY("update_priority", "更新优先级"),
    REPLY("reply", "回复"),
    DELETE("delete", "删除");
    
    private final String code;
    private final String description;
    
    public static FeedbackOperationType fromCode(String code) {
        for (FeedbackOperationType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return CREATE;
    }
}
