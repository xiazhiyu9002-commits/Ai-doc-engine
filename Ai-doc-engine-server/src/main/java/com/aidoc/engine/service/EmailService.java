package com.aidoc.engine.service;

public interface EmailService {
    
    void sendPasswordResetEmail(String email, String token);
    
    void sendWelcomeEmail(String email, String username);
    
    void sendFeedbackThankEmail(String email, String title, String feedbackId);
    
    void sendFeedbackResolvedEmail(String email, String title, String reply, String feedbackId);
    
    void sendFeedbackUpdateEmail(String email, String title, String updateContent, String feedbackId);
}
