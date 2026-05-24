package com.aidoc.engine.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模板类型枚举
 */
@Getter
@AllArgsConstructor
public enum TemplateType {
    
    SYSTEM("system", "系统模板"),
    CUSTOM("custom", "自定义模板");
    
    private final String code;
    private final String description;
    
    public static TemplateType fromCode(String code) {
        for (TemplateType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return CUSTOM;
    }
}
