package com.aidoc.engine.model.vo.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackStatsVO {
    
    private Long totalFeedbacks;
    
    private Long pendingFeedbacks;
    
    private Long processingFeedbacks;
    
    private Long resolvedFeedbacks;
    
    private Long closedFeedbacks;
    
    private Long todayFeedbacks;
    
    private Long weekFeedbacks;
    
    private Long monthFeedbacks;
    
    private Long bugCount;
    
    private Long suggestionCount;
    
    private Long featureCount;
    
    private Long highPriorityCount;
}
