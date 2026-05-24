package com.aidoc.engine.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OCR识别状态枚举
 */
@Getter
@AllArgsConstructor
public enum OcrStatus {
    
    RECOGNIZED("RECOGNIZED", "识别成功"),
    FAILED("FAILED", "识别失败");
    
    private final String code;
    private final String description;
    
    public static OcrStatus fromCode(String code) {
        for (OcrStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
