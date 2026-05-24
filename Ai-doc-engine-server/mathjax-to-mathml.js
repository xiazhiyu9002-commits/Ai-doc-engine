#!/usr/bin/env node

/**
 * MathJax LaTeX to MathML 转换器
 * 使用 MathJax 3.x 正确的 API
 */

const { mathjax } = require('mathjax-full/js/mathjax.js');
const { TeX } = require('mathjax-full/js/input/tex.js');
const { liteAdaptor } = require('mathjax-full/js/adaptors/liteAdaptor.js');
const { RegisterHTMLHandler } = require('mathjax-full/js/handlers/html.js');
const { AllPackages } = require('mathjax-full/js/input/tex/AllPackages.js');
const { SerializedMmlVisitor } = require('mathjax-full/js/core/MmlTree/SerializedMmlVisitor.js');
const { SVG } = require('mathjax-full/js/output/svg.js');

// 从命令行参数读取 LaTeX
const latex = process.argv[2];
const inline = String(process.argv[3] || '').toLowerCase() === 'true';

if (!latex) {
    console.error('Usage: node mathjax-to-mathml.js "<latex>" [inline=true|false]');
    process.exit(1);
}

try {
    // 创建适配器
    const adaptor = liteAdaptor();
    const handler = RegisterHTMLHandler(adaptor);
    
    // 配置 TeX 输入
    const tex = new TeX({
        packages: AllPackages
    });
    
    // 配置 SVG 输出（仅用于初始化文档上下文，最终 MathML 由 SerializedMmlVisitor 生成）
    const svg = new SVG({
        fontCache: 'none'
    });
    
    // 创建文档
    const html = mathjax.document('', {
        InputJax: tex,
        OutputJax: svg
    });
    
    // 编译 LaTeX 到内部 MathJax MmlTree
    const compiled = tex.compile({
        math: latex,
        display: !inline,
        inputData: {}
    }, html);
    if (!compiled) {
        throw new Error('Conversion failed: no root node');
    }

    // 使用官方序列化器生成 MathML 字符串
    const visitor = new SerializedMmlVisitor();
    let mathml = visitor.visitTree(compiled);
    if (mathml && mathml.trim()) {
        const displayValue = inline ? 'inline' : 'block';

        if (mathml.includes('display="block"') || mathml.includes('display="inline"')) {
            mathml = mathml.replace(/display="(block|inline)"/g, `display="${displayValue}"`);
        } else {
            mathml = mathml.replace('<math', `<math display="${displayValue}"`);
        }

        console.log(mathml);
    } else {
        // 兜底：使用旧的手动构建方式
        const fallback = buildMathML(compiled);
        if (!fallback || !fallback.trim()) {
            throw new Error('Conversion failed: no root node');
        }
        console.log(fallback);
    }
    
} catch (e) {
    console.error('MathJax Error:', e && e.stack ? e.stack : (e && e.message ? e.message : String(e)));
    process.exit(1);
}

/**
 * 从 MathJax 内部节点构建 MathML
 */
function buildMathML(node) {
    const xmlns = 'http://www.w3.org/1998/Math/MathML';
    
    function toMML(n) {
        if (!n) return '';
        
        const kind = n.kind || 'mrow';
        
        // 文本节点
        if (kind === 'text' || n.text !== undefined) {
            return escapeXml(n.text || '');
        }
        
        // 获取属性
        const attrs = n.attributes || {};
        let attrStr = '';
        for (const [key, val] of Object.entries(attrs)) {
            if (val !== undefined && val !== null && val !== '') {
                attrStr += ` ${key}="${escapeXml(String(val))}"`;
            }
        }
        
        // 获取子节点
        let children = '';
        if (n.childNodes && n.childNodes.length > 0) {
            children = n.childNodes.map(toMML).join('');
        } else if (n.children && n.children.length > 0) {
            children = n.children.map(toMML).join('');
        }
        
        return `<${kind}${attrStr}>${children}</${kind}>`;
    }
    
    const inner = toMML(node);
    return `<math xmlns="${xmlns}" display="block">${inner}</math>`;
}

/**
 * XML 转义
 */
function escapeXml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&apos;');
}
