package com.aidoc.engine.model.udm.block;

import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.content.FormulaContent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 公式 Block
 */
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FormulaBlock extends UdmBlock {
    
    /**
     * 公式内容
     */
    private FormulaContent content;
    
    public FormulaBlock(FormulaContent content) {
        this.setType("formula");
        this.content = content;
    }
}
