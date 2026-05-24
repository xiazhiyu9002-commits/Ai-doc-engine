package com.aidoc.engine.service.impl;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.config.FileStorageProperties;
import com.aidoc.engine.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {
    
    private final FileStorageProperties fileStorageProperties;
    
    @Override
    public String storeFile(MultipartFile file, String subDir) {
        validateFile(file);
        
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }
            
            String newFilename = UUID.randomUUID().toString() + extension;
            
            Path uploadPath = Paths.get(fileStorageProperties.getUploadDir(), subDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            
            Path targetLocation = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("文件存储成功: {}", targetLocation.toString());
            
            return getFileUrl(newFilename, subDir);
            
        } catch (IOException e) {
            log.error("文件存储失败", e);
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR, "文件存储失败: " + e.getMessage());
        }
    }
    
    @Override
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件不能为空");
        }
        
        if (file.getSize() > fileStorageProperties.getMaxSize()) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED, 
                    "文件大小超过限制，最大允许 " + (fileStorageProperties.getMaxSize() / 1024 / 1024) + "MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件名不能为空");
        }
        
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase().replace(".", "");
        }
        
        if (!fileStorageProperties.getAllowedExtensionList().contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, 
                    "不支持的文件类型，仅支持: " + fileStorageProperties.getAllowedExtensions());
        }
    }
    
    @Override
    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "图片不能为空");
        }
        
        if (file.getSize() > fileStorageProperties.getImageMaxSize()) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED, 
                    "图片大小超过限制，最大允许 " + (fileStorageProperties.getImageMaxSize() / 1024 / 1024) + "MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "图片名不能为空");
        }
        
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase().replace(".", "");
        }
        
        List<String> imageExtensions = List.of("jpg", "jpeg", "png", "gif", "webp");
        if (!imageExtensions.contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, 
                    "不支持的图片类型，仅支持: jpg, jpeg, png, gif, webp");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "上传的文件不是有效的图片");
        }
    }
    
    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        try {
            String urlPrefix = fileStorageProperties.getNormalizedUrlPrefix();
            String relativePath = fileUrl;
            if (fileUrl.startsWith(urlPrefix + "/")) {
                relativePath = fileUrl.substring(urlPrefix.length() + 1);
            }
            
            Path filePath = Paths.get(fileStorageProperties.getUploadDir())
                    .resolve(relativePath)
                    .toAbsolutePath()
                    .normalize();
            
            Files.deleteIfExists(filePath);
            log.info("文件删除成功: {}", filePath.toString());
            
        } catch (IOException e) {
            log.warn("文件删除失败: {}", fileUrl, e);
        }
    }
    
    @Override
    public String getFileUrl(String filename, String subDir) {
        return fileStorageProperties.getNormalizedUrlPrefix() + "/" + subDir + "/" + filename;
    }
    
    @Override
    public List<String> storeFiles(List<MultipartFile> files, String subDir) {
        List<String> urls = new ArrayList<>();
        
        if (files == null || files.isEmpty()) {
            return urls;
        }
        
        if (files.size() > fileStorageProperties.getMaxImageCount()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, 
                    "图片数量超过限制，最多允许上传 " + fileStorageProperties.getMaxImageCount() + " 张");
        }
        
        for (MultipartFile file : files) {
            validateImageFile(file);
            String url = storeFile(file, subDir);
            urls.add(url);
        }
        
        return urls;
    }
}
