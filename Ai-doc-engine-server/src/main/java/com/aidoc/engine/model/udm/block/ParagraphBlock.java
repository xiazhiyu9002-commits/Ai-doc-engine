package com.aidoc.engine.model.udm.block;

import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.content.ParagraphContent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 段落 Block
 */
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ParagraphBlock extends UdmBlock {
    
    /**
     * 段落内容
     */
    private ParagraphContent content;
    
    public ParagraphBlock(ParagraphContent content) {
        this.setType("paragraph");
        this.content = content;
    }
}
