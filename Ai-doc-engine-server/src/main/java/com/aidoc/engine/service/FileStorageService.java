package com.aidoc.engine.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {
    
    String storeFile(MultipartFile file, String subDir);
    
    void validateFile(MultipartFile file);
    
    void validateImageFile(MultipartFile file);
    
    void deleteFile(String fileUrl);
    
    String getFileUrl(String filename, String subDir);
    
    List<String> storeFiles(List<MultipartFile> files, String subDir);
}
