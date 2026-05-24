package com.aidoc.engine.service;

/**
 * Mermaid 渲染服务
 * 将 Mermaid 代码渲染为图片
 */
public interface MermaidRenderService {
    
    /**
     * 将 Mermaid 代码渲染为 PNG 图片
     * 
     * @param mermaidCode Mermaid 代码
     * @return PNG 图片字节数组
     */
    byte[] renderToPng(String mermaidCode);
    
    /**
     * 将 Mermaid 代码渲染为 Base64 编码的图片
     * 
     * @param mermaidCode Mermaid 代码
     * @return Base64 编码的图片字符串
     */
    String renderToBase64(String mermaidCode);
}
