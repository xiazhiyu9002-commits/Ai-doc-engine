package com.aidoc.engine.model.udm.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码块内容
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeBlockContent {
    
    /**
     * 代码语言
     */
    private String language;
    
    /**
     * 代码内容
     */
    private String code;
}
