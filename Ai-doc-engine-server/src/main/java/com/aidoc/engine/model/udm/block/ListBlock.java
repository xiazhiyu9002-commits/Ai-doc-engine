package com.aidoc.engine.model.udm.block;

import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.content.ListContent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 列表 Block
 */
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListBlock extends UdmBlock {
    
    /**
     * 列表内容
     */
    private ListContent content;
    
    public ListBlock(ListContent content) {
        this.setType("list");
        this.content = content;
    }
}
