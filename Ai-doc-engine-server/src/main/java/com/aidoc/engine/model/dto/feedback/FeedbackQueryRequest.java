package com.aidoc.engine.model.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackQueryRequest {
    
    @Builder.Default
    private Integer page = 1;
    
    @Builder.Default
    private Integer size = 10;
    
    private String status;
    
    private String feedbackType;
    
    private String priority;
    
    private String keyword;
    
    private Long userId;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
}
