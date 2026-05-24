package com.aidoc.engine.renderer;

import com.aidoc.engine.model.dto.template.TemplateConfig;
import com.aidoc.engine.model.udm.UdmBlock;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;

/**
 * Block渲染器接口
 * 
 * @param <T> Block类型
 */
public interface BlockRenderer<T extends UdmBlock> {
    
    /**
     * 渲染Block（不使用模板）
     * @param block Block对象
     * @param wordPackage Word文档包
     * @return 段落对象
     */
    P render(T block, WordprocessingMLPackage wordPackage);
    
    /**
     * 渲染Block（使用模板配置）
     * 
     * @param block Block对象
     * @param wordPackage Word文档包
     * @param templateConfig 模板配置
     * @return 段落对象
     */
    default P render(T block, WordprocessingMLPackage wordPackage, TemplateConfig templateConfig) {
        // 默认实现：忽略模板配置，调用不带模板的render方法
        return render(block, wordPackage);
    }
}
