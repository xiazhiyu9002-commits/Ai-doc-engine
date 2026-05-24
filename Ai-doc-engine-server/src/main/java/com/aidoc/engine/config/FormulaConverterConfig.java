package com.aidoc.engine.config;

import com.aidoc.engine.service.FormulaConvertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 公式转换器配置
 * 使用 MathJax 作为公式转换引擎
 * 
 * MathJax 优势：
 * - 支持数学、化学、物理等多学科公式
 * - 支持 mhchem 扩展（化学方程式）
 * - 支持 physics 扩展（物理符号）
 * - 更完整的 LaTeX 兼容性
 */
@Slf4j
@Configuration
public class FormulaConverterConfig {
    
    /**
     * 配置公式转换服务，使用 MathJax
     */
    @Bean
    @Primary
    public FormulaConvertService formulaConvertService(
            @Qualifier("mathJaxFormulaConvertService") FormulaConvertService mathJaxService) {
        
        log.info("使用 MathJax 作为公式转换引擎（支持数学、化学、物理等多学科公式）");
        return mathJaxService;
    }
}
