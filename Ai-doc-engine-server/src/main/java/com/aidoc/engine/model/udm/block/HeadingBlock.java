package com.aidoc.engine.model.udm.block;

import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.content.HeadingContent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 标题 Block
 */
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HeadingBlock extends UdmBlock {
    
    /**
     * 标题内容
     */
    private HeadingContent content;
    
    public HeadingBlock(HeadingContent content) {
        this.setType("heading");
        this.content = content;
    }
}
