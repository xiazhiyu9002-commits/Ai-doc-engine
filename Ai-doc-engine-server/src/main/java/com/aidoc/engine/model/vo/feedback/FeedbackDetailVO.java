package com.aidoc.engine.model.vo.feedback;

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
public class FeedbackDetailVO {
    
    private Long id;
    
    private Long userId;
    
    private String username;
    
    private String userEmail;
    
    private String content;
    
    private String feedbackType;
    
    private String status;
    
    private String priority;
    
    private List<String> imageUrls;
    
    private String processedByName;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
