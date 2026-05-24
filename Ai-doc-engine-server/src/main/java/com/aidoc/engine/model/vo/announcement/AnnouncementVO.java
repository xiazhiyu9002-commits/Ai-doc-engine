package com.aidoc.engine.model.vo.announcement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementVO {
    
    private Long id;
    
    private String title;
    
    private String content;
    
    private String announcementType;
    
    private List<String> imageUrls;
    
    private Boolean isPublished;
    
    private LocalDateTime publishedAt;
    
    private LocalDateTime expireAt;
    
    private String createdByName;
    
    private Long createdBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
