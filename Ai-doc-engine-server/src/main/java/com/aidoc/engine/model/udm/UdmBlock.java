package com.aidoc.engine.model.udm;

import com.aidoc.engine.enums.BlockType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = com.aidoc.engine.model.udm.block.HeadingBlock.class, name = "heading"),
    @JsonSubTypes.Type(value = com.aidoc.engine.model.udm.block.ParagraphBlock.class, name = "paragraph"),
    @JsonSubTypes.Type(value = com.aidoc.engine.model.udm.block.ListBlock.class, name = "list"),
    @JsonSubTypes.Type(value = com.aidoc.engine.model.udm.block.TableBlock.class, name = "table"),
    @JsonSubTypes.Type(value = com.aidoc.engine.model.udm.block.FormulaBlock.class, name = "formula"),
    @JsonSubTypes.Type(value = com.aidoc.engine.model.udm.block.FlowchartBlock.class, name = "flowchart"),
    @JsonSubTypes.Type(value = com.aidoc.engine.model.udm.block.CodeBlock.class, name = "codeblock"),
    @JsonSubTypes.Type(value = com.aidoc.engine.model.udm.block.TaskListBlock.class, name = "tasklist"),
    @JsonSubTypes.Type(value = com.aidoc.engine.model.udm.block.FootnoteBlock.class, name = "footnote")
})
public abstract class UdmBlock {
    
    private String type;
    
    private Map<String, Object> metadata = new HashMap<>();
    
    private int sourceStartOffset = -1;
    
    private int sourceEndOffset = -1;
    
    private int sourceStartLine = -1;
    
    private int sourceEndLine = -1;
    
    public BlockType getBlockType() {
        return BlockType.fromCode(type);
    }
}
