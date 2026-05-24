package com.aidoc.engine.model.udm.block;

import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.content.TaskListContent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 任务列表 Block
 */
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaskListBlock extends UdmBlock {
    
    private TaskListContent content;
    
    public TaskListBlock(TaskListContent content) {
        this.setType("tasklist");
        this.content = content;
    }
}
