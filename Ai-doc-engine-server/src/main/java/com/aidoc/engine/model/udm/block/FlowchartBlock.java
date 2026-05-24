package com.aidoc.engine.model.udm.block;

import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.content.FlowchartContent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 流程图 Block
 */
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FlowchartBlock extends UdmBlock {
    
    /**
     * 流程图内容
     */
    private FlowchartContent content;
    
    public FlowchartBlock(FlowchartContent content) {
        this.setType("flowchart");
        this.content = content;
    }
}
