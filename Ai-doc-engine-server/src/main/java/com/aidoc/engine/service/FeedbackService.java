package com.aidoc.engine.service;

import com.aidoc.engine.model.dto.feedback.FeedbackQueryRequest;
import com.aidoc.engine.model.dto.feedback.FeedbackSubmitRequest;
import com.aidoc.engine.model.dto.feedback.FeedbackUpdateRequest;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.feedback.FeedbackDetailVO;
import com.aidoc.engine.model.vo.feedback.FeedbackStatsVO;
import com.aidoc.engine.model.vo.feedback.FeedbackVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FeedbackService {
    
    FeedbackVO submitFeedback(FeedbackSubmitRequest request, List<MultipartFile> images, Long userId, String ip);
    
    PageResult<FeedbackVO> getFeedbackList(FeedbackQueryRequest request);
    
    FeedbackDetailVO getFeedbackDetail(Long id, Long currentUserId, boolean isAdmin);
    
    FeedbackVO updateFeedbackStatus(Long id, FeedbackUpdateRequest request, Long operatorId, String ip);
    
    FeedbackVO replyFeedback(Long id, String reply, Long operatorId, String ip);
    
    PageResult<FeedbackVO> getUserFeedbacks(Long userId, int page, int size);
    
    FeedbackStatsVO getFeedbackStats();
    
    void deleteFeedback(Long id, Long operatorId, String ip);
}
