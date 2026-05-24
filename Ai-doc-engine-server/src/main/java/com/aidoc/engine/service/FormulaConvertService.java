package com.aidoc.engine.service;

/**
 * 公式转换服务接口
 */
public interface FormulaConvertService {
    
    /**
     * 将 LaTeX 转换为 MathML
     * 
     * @param latex LaTeX 公式
     * @return MathML 字符串
     */
    String convertLatexToMathml(String latex);
    
    /**
     * 将 LaTeX 转换为 MathML（支持指定是否为行内公式）
     * 
     * @param latex LaTeX 公式
     * @param inline 是否为行内公式
     * @return MathML 字符串
     */
    String convertLatexToMathml(String latex, boolean inline);
    
    /**
     * 将 MathML 转换为 OMML
     * 
     * @param mathml MathML 字符串
     * @return OMML 字符串
     */
    String convertMathmlToOmml(String mathml);
    
    /**
     * 将 LaTeX 直接转换为 OMML
     * 
     * @param latex LaTeX 公式
     * @return OMML 字符串
     */
    String convertLatexToOmml(String latex);
    
    /**
     * 将 LaTeX 直接转换为 OMML（支持指定是否为行内公式）
     * 
     * @param latex LaTeX 公式
     * @param inline 是否为行内公式
     * @return OMML 字符串
     */
    String convertLatexToOmml(String latex, boolean inline);
}
