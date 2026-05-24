package com.aidoc.engine.model.udm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一文档模型（UDM）- 文档根对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UdmDocument {
    
    /**
     * 文档块列表
     */
    @Builder.Default
    private List<UdmBlock> blocks = new ArrayList<>();
    
    /**
     * 文档元数据
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
