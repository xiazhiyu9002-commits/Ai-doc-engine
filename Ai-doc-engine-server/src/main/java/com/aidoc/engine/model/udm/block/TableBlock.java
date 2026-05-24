package com.aidoc.engine.model.udm.block;

import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.content.TableContent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 表格 Block
 */
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TableBlock extends UdmBlock {
    
    /**
     * 表格内容
     */
    private TableContent content;
    
    public TableBlock(TableContent content) {
        this.setType("table");
        this.content = content;
    }
}
