package com.aidoc.engine.parser;

import com.aidoc.engine.model.udm.UdmDocument;

/**
 * 文档解析器接口
 */
public interface DocumentParser {
    
    /**
     * 解析文档为 UDM
     * 
     * @param source 源文档内容
//     * @return UDM 文档
     */
    UdmDocument parse(String source);
}
