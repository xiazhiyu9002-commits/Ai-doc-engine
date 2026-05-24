package com.aidoc.engine.service.impl;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.config.MermaidProperties;
import com.aidoc.engine.service.MermaidRenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Mermaid 渲染服务实现
 * 使用 mermaid-cli (mmdc) 将 Mermaid 代码渲染为图片
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MermaidRenderServiceImpl implements MermaidRenderService {
    
    private final MermaidProperties mermaidProperties;
    
    @Override
    public byte[] renderToPng(String mermaidCode) {
        if (mermaidCode == null || mermaidCode.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "Mermaid 代码不能为空");
        }
        
        Path inputFile = null;
        Path outputFile = null;
        
        try {
            // 创建临时文件
            inputFile = Files.createTempFile("mermaid-", ".mmd");
            outputFile = Files.createTempFile("mermaid-", ".png");
            
            // 写入 Mermaid 代码
            Files.writeString(inputFile, mermaidCode, StandardCharsets.UTF_8);
            
            // 执行 mmdc 命令
            String command = mermaidProperties.getCommand();
            log.debug("使用 Mermaid 命令: {}", command);
            
            ProcessBuilder processBuilder = new ProcessBuilder(
                command,
                "-i", inputFile.toString(),
                "-o", outputFile.toString(),
                "-b", mermaidProperties.getBackground(),
                "-t", mermaidProperties.getTheme()
            );
            
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // 等待进程完成
            int timeoutSeconds = mermaidProperties.getTimeout();
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException(ErrorCode.EXPORT_FAILED, 
                    "Mermaid 渲染超时（超过 " + timeoutSeconds + " 秒）");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("Mermaid 渲染失败，退出码: {}, 输出: {}", exitCode, output);
                throw new BusinessException(ErrorCode.EXPORT_FAILED, 
                    "Mermaid 渲染失败: " + output);
            }
            
            // 读取生成的图片
            byte[] imageBytes = Files.readAllBytes(outputFile);
            log.info("Mermaid 渲染成功，图片大小: {} bytes", imageBytes.length);
            
            return imageBytes;
            
        } catch (IOException e) {
            log.error("Mermaid 渲染 IO 错误", e);
            throw new BusinessException(ErrorCode.EXPORT_FAILED, 
                "Mermaid 渲染失败: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Mermaid 渲染被中断", e);
            throw new BusinessException(ErrorCode.EXPORT_FAILED, 
                "Mermaid 渲染被中断");
        } finally {
            // 清理临时文件
            cleanupTempFile(inputFile);
            cleanupTempFile(outputFile);
        }
    }
    
    @Override
    public String renderToBase64(String mermaidCode) {
        byte[] imageBytes = renderToPng(mermaidCode);
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    private void cleanupTempFile(Path file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                log.warn("清理临时文件失败: {}", file, e);
            }
        }
    }
}
