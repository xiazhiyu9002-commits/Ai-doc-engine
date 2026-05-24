package com.aidoc.engine;

import com.aidoc.engine.config.AppOcrProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * AI Doc Engine 应用启动类
 *
 * 功能：AI Markdown → 可编辑 Word 文档系统
 *
 * 核心能力：
 * 1. Markdown 解析为 UDM（统一文档模型）
 * 2. 公式转换：LaTeX → OMML（Word 原生公式）
 * 3. 流程图转换：JSON → Mermaid CLI（Word 图形对象）
 * 4. Word 文档导出（使用 docx4j）
 *
 * 技术栈：
 * - Spring Boot 3.x
 * - Spring Security + JWT
 * - Spring Data JPA
 * - flexmark-java（Markdown 解析）
 * - docx4j（Word 生成）
 *
 * @author AI Doc Engine Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(AppOcrProperties.class)
public class AiDocEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiDocEngineApplication.class, args);

        System.out.println("""
                 
                    █████╗ ██╗    ██████╗  ██████╗  ██████╗     ███████╗███╗   ██╗ ██████╗
                   ██╔══██╗██║    ██╔══██╗██╔═══██╗██╔════╝     ██╔════╝████╗  ██║██╔════╝
                   ███████║██║    ██║  ██║██║   ██║██║          █████╗  ██╔██╗ ██║██║  ███╗
                   ██╔══██║██║    ██║  ██║██║   ██║██║   ██║    ██╔══╝  ██║╚██╗██║██║  ██║  
                   ██║  ██║██║    ██████╔╝╚██████╔╝╚██████╔╝    ███████╗██║ ╚████║╚██████╗
                   ╚═╝  ╚═╝╚═╝    ╚═════╝  ╚═════╝  ╚═════╝     ╚══════╝╚═╝  ╚═══╝ ╚═════╝
                 
                ================================================================
                                        AI Doc Engine
                                    智能文档转换引擎 · 启动成功
                ================================================================
                🔹 项目名称：AI 文档转换引擎
                🔹 核心能力：Markdown → 可编辑 Word 文档
                🔹 文档模型：UDM（统一文档模型）
                🔹 公式支持：LaTeX → OMML（Word 原生公式）
                🔹 图表支持：Mermaid → Word 图形对象
                🔹 导出引擎：docx4j
                🔹 认证方式：Spring Security + JWT
                ================================================================
                """);
    }
}