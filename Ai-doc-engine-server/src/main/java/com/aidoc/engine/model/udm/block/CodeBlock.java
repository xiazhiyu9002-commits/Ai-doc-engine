package com.aidoc.engine.model.udm.block;

import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.content.CodeBlockContent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 代码块 Block
 */
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CodeBlock extends UdmBlock {
    
    /**
     * 代码块内容
     */
    private CodeBlockContent content;
    
    public CodeBlock(CodeBlockContent content) {
        this.setType("codeblock");
        this.content = content;
    }
}
