package com.aidoc.engine.model.dto.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackSubmitRequest {
    
    @NotBlank(message = "内容不能为空")
    @Size(max = 5000, message = "内容长度不能超过5000字符")
    private String content;
    
    private String feedbackType;
}
