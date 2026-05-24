package com.aidoc.engine.parser;

import com.aidoc.engine.model.udm.UdmDocument;
import com.aidoc.engine.model.udm.block.CodeBlock;
import com.aidoc.engine.parser.impl.ExtendedMarkdownDocumentParser;
import com.aidoc.engine.service.MermaidParserService;
import com.aidoc.engine.service.impl.MermaidParserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * 代码块解析测试
 */
@Slf4j
public class CodeBlockParseTest {
    
    private final MermaidParserService mermaidParserService = new MermaidParserServiceImpl();
    private final ExtendedMarkdownDocumentParser parser = new ExtendedMarkdownDocumentParser(mermaidParserService);
    
    @Test
    public void testCodeBlockWithLanguage() {
        String markdown = "### 4. C / C++\n\n" +
                "```c\n" +
                "int square(int x) {\n" +
                "    return x * x;\n" +
                "}\n" +
                "```\n";
        
        log.info("========== 测试代码块解析 ==========");
        log.info("输入 Markdown:\n{}", markdown);
        
        UdmDocument udm = parser.parse(markdown);
        
        log.info("解析结果：{} 个 Block", udm.getBlocks().size());
        
        udm.getBlocks().forEach(block -> {
            log.info("Block 类型: {}", block.getType());
            if (block instanceof CodeBlock) {
                CodeBlock codeBlock = (CodeBlock) block;
                log.info("  语言: {}", codeBlock.getContent().getLanguage());
                log.info("  代码:\n{}", codeBlock.getContent().getCode());
            }
        });
    }
    
    @Test
    public void testMultipleCodeBlocks() {
        String markdown = "### 1. Python\n\n" +
                "```python\n" +
                "def square(x):\n" +
                "    return x ** 2\n" +
                "```\n\n" +
                "### 2. Java\n\n" +
                "```java\n" +
                "public static double square(double x) {\n" +
                "    return x * x;\n" +
                "}\n" +
                "```\n\n" +
                "### 3. JavaScript\n\n" +
                "```javascript\n" +
                "function square(x) {\n" +
                "    return x * x;\n" +
                "}\n" +
                "```\n\n" +
                "### 4. C / C++\n\n" +
                "```c\n" +
                "int square(int x) {\n" +
                "    return x * x;\n" +
                "}\n" +
                "```\n";
        
        log.info("========== 测试多个代码块解析 ==========");
        log.info("输入 Markdown 长度: {}", markdown.length());
        
        UdmDocument udm = parser.parse(markdown);
        
        log.info("解析结果：{} 个 Block", udm.getBlocks().size());
        
        long codeBlockCount = udm.getBlocks().stream()
                .filter(block -> block instanceof CodeBlock)
                .count();
        
        log.info("代码块数量: {}", codeBlockCount);
        
        udm.getBlocks().stream()
                .filter(block -> block instanceof CodeBlock)
                .map(block -> (CodeBlock) block)
                .forEach(codeBlock -> {
                    log.info("代码块:");
                    log.info("  语言: {}", codeBlock.getContent().getLanguage());
                    log.info("  代码长度: {}", codeBlock.getContent().getCode().length());
                    log.info("  代码:\n{}", codeBlock.getContent().getCode());
                    log.info("---");
                });
    }
    
    @Test
    public void testCodeBlockWithFormula() {
        String markdown = "### 4. C / C++\n\n" +
                "```c\n" +
                "int square(int x) {\n" +
                "    return x * x;\n" +
                "}\n" +
                "```\n\n" +
                "### 5. 数学公式\n\n" +
                "\\[f(x) = x^2\\]\n";
        
        log.info("========== 测试代码块和公式混合解析 ==========");
        log.info("输入 Markdown:\n{}", markdown);
        
        UdmDocument udm = parser.parse(markdown);
        
        log.info("解析结果：{} 个 Block", udm.getBlocks().size());
        
        udm.getBlocks().forEach(block -> {
            log.info("Block 类型: {}, 内容类: {}", 
                    block.getType(), 
                    block.getClass().getSimpleName());
        });
    }
}
