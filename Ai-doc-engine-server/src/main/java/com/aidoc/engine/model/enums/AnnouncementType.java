package com.aidoc.engine.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AnnouncementType {
    
    NOTICE("notice", "通知"),
    THANKS("thanks", "感谢信"),
    UPDATE("update", "更新日志");
    
    private final String code;
    private final String description;
    
    public static AnnouncementType fromCode(String code) {
        for (AnnouncementType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return NOTICE;
    }
}
