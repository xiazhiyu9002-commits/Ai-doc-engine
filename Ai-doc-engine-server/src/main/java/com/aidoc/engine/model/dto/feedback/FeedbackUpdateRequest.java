package com.aidoc.engine.model.dto.feedback;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackUpdateRequest {
    
    private String status;
    
    private String priority;
    
    @Size(max = 2000, message = "回复内容长度不能超过2000字符")
    private String adminReply;
}
