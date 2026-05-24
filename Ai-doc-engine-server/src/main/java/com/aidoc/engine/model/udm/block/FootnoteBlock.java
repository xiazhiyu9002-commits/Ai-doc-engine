package com.aidoc.engine.model.udm.block;

import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.content.FootnoteContent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 脚注 Block
 */
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FootnoteBlock extends UdmBlock {
    
    private FootnoteContent content;
    
    public FootnoteBlock(FootnoteContent content) {
        this.setType("footnote");
        this.content = content;
    }
}
