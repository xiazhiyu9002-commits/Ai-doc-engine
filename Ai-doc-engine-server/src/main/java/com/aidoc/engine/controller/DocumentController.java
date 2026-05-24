package com.aidoc.engine.controller;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.common.response.ApiResponse;
import com.aidoc.engine.exporter.WordExporter;
import com.aidoc.engine.model.dto.template.TemplateConfig;
import com.aidoc.engine.model.entity.ExportTaskEntity;
import com.aidoc.engine.model.entity.TemplateEntity;
import com.aidoc.engine.model.entity.UserEntity;
import com.aidoc.engine.model.udm.UdmDocument;
import com.aidoc.engine.parser.DocumentParser;
import com.aidoc.engine.repository.ExportTaskRepository;
import com.aidoc.engine.repository.TemplateRepository;
import com.aidoc.engine.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
public class DocumentController {
    
    private final DocumentParser documentParser;
    private final WordExporter wordExporter;
    private final ExportTaskRepository exportTaskRepository;
    private final TemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    @Data
    public static class ParseRequest {
        @NotBlank(message = "Markdown内容不能为空")
        private String markdown;
        private Long templateId;
        private String fileName;
    }
    
    @Data
    public static class ExportRequest {
        private UdmDocument udm;
        private Long templateId;
        private String fileName;
    }
    
    @PostMapping("/parse")
    public ApiResponse<UdmDocument> parseMarkdown(@Valid @RequestBody ParseRequest request) {
        log.info("解析 Markdown 文档，长度: {}", request.getMarkdown().length());
        UdmDocument document = documentParser.parse(request.getMarkdown());
        return ApiResponse.success(document);
    }
    
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportWord(@Valid @RequestBody ExportRequest request) {
        Long userId = getCurrentUserId();
        UdmDocument document = request.getUdm();
        return doExport(document, request.getTemplateId(), request.getFileName(), userId);
    }
    
    @PostMapping("/export/word/from-markdown")
    public ResponseEntity<byte[]> exportWordFromMarkdown(@Valid @RequestBody ParseRequest request) {
        Long userId = getCurrentUserId();
        log.info("从 Markdown 直接导出 Word，用户: {}, 文件名: {}", userId, request.getFileName());
        
        UdmDocument document = documentParser.parse(request.getMarkdown());
        return doExport(document, request.getTemplateId(), request.getFileName(), userId);
    }
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            UserEntity user = userRepository.findByUsername(username).orElse(null);
            return user != null ? user.getId() : null;
        }
        return null;
    }
    
    private ResponseEntity<byte[]> doExport(UdmDocument document, Long templateId, String fileName, Long userId) {
        LocalDateTime startTime = LocalDateTime.now();
        ExportTaskEntity taskEntity = null;
        
        if (userId != null) {
            taskEntity = ExportTaskEntity.builder()
                    .userId(userId)
                    .templateId(templateId)
                    .fileName(fileName != null ? fileName + ".docx" : null)
                    .taskStatus("PROCESSING")
                    .startTime(startTime)
                    .build();
            exportTaskRepository.save(taskEntity);
        }
        
        try {
            TemplateConfig templateConfig = null;
            if (templateId != null) {
                TemplateEntity template = templateRepository.findById(templateId).orElse(null);
                if (template != null && template.getConfigJson() != null) {
                    templateConfig = objectMapper.readValue(template.getConfigJson(), TemplateConfig.class);
                }
            }
            
            byte[] wordBytes = wordExporter.export(document, templateConfig);
            
            LocalDateTime endTime = LocalDateTime.now();
            int processingTimeMs = (int) java.time.Duration.between(startTime, endTime).toMillis();
            
            if (taskEntity != null) {
                taskEntity.setTaskStatus("SUCCESS");
                taskEntity.setFileName(fileName != null ? fileName + ".docx" : "document.docx");
                taskEntity.setFileSize((long) wordBytes.length);
                taskEntity.setCharCount(document.getBlocks() != null ? document.getBlocks().size() : 0);
                taskEntity.setEndTime(endTime);
                taskEntity.setProcessingTimeMs(processingTimeMs);
                exportTaskRepository.save(taskEntity);
            }
            
            String downloadFileName = fileName != null ? fileName + ".docx" : "document.docx";
            String encodedFileName = URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            
            log.info("导出成功，文件名: {}, 大小: {} bytes, 耗时: {} ms", 
                    downloadFileName, wordBytes.length, processingTimeMs);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename*=UTF-8''" + encodedFileName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(wordBytes);
                    
        } catch (BusinessException e) {
            if (taskEntity != null) {
                taskEntity.setTaskStatus("FAILED");
                taskEntity.setErrorMessage(e.getMessage());
                taskEntity.setEndTime(LocalDateTime.now());
                exportTaskRepository.save(taskEntity);
            }
            throw e;
        } catch (Exception e) {
            log.error("导出失败", e);
            if (taskEntity != null) {
                taskEntity.setTaskStatus("FAILED");
                taskEntity.setErrorMessage(e.getMessage());
                taskEntity.setEndTime(LocalDateTime.now());
                exportTaskRepository.save(taskEntity);
            }
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "导出失败: " + e.getMessage());
        }
    }
}
