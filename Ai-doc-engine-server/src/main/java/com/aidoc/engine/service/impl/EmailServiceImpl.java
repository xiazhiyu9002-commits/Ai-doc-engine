package com.aidoc.engine.service.impl;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@aidoc.com}")
    private String fromEmail;
    
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;
    
    @Override
    public void sendPasswordResetEmail(String email, String token) {
        log.info("发送密码重置邮件: email={}", email);
        
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            
            String subject = "AI Doc Engine - 密码重置";
            String content = String.format(
                    "您好，\n\n" +
                    "您请求重置密码。请点击以下链接重置您的密码：\n\n" +
                    "%s\n\n" +
                    "此链接将在 10 分钟后过期。\n\n" +
                    "如果您没有请求重置密码，请忽略此邮件。\n\n" +
                    "AI Doc Engine 团队",
                    resetUrl
            );
            
            sendSimpleEmail(email, subject, content);
            
            log.info("密码重置邮件发送成功: email={}", email);
            
        } catch (Exception e) {
            log.error("密码重置邮件发送失败", e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_ERROR, "邮件发送失败: " + e.getMessage());
        }
    }
    
    @Override
    public void sendWelcomeEmail(String email, String username) {
        log.info("发送欢迎邮件: email={}, username={}", email, username);
        
        try {
            String subject = "欢迎使用 AI Doc Engine";
            String content = String.format(
                    "您好 %s，\n\n" +
                    "欢迎注册 AI Doc Engine！\n\n" +
                    "您现在可以使用以下功能：\n" +
                    "- Markdown 文档解析\n" +
                    "- Word 文档导出\n" +
                    "- 公式识别和转换\n" +
                    "- 流程图绘制\n\n" +
                    "开始使用：%s\n\n" +
                    "AI Doc Engine 团队",
                    username,
                    frontendUrl
            );
            
            sendSimpleEmail(email, subject, content);
            
            log.info("欢迎邮件发送成功: email={}", email);
            
        } catch (Exception e) {
            log.warn("欢迎邮件发送失败（非关键错误）", e);
            // 欢迎邮件发送失败不抛出异常
        }
    }
    
    /**
     * 发送简单文本邮件
     */
    private void sendSimpleEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        
        mailSender.send(message);
    }
    
    @Override
    public void sendFeedbackThankEmail(String email, String title, String feedbackId) {
        log.info("发送反馈感谢邮件: email={}, feedbackId={}", email, feedbackId);
        
        try {
            String subject = "感谢您的反馈 - AI Doc Engine";
            String content = String.format(
                    "您好，\n\n" +
                    "感谢您对 AI Doc Engine 的关注和支持！\n\n" +
                    "您的反馈「%s」已收到，我们会尽快处理。\n\n" +
                    "反馈编号：%s\n\n" +
                    "再次感谢您的宝贵意见！\n\n" +
                    "AI Doc Engine 团队",
                    title,
                    feedbackId
            );
            
            sendSimpleEmail(email, subject, content);
            
            log.info("反馈感谢邮件发送成功: email={}", email);
            
        } catch (Exception e) {
            log.warn("反馈感谢邮件发送失败（非关键错误）", e);
        }
    }
    
    @Override
    public void sendFeedbackResolvedEmail(String email, String title, String reply, String feedbackId) {
        log.info("发送反馈处理完成邮件: email={}, feedbackId={}", email, feedbackId);
        
        try {
            String subject = "您的反馈已处理完成 - AI Doc Engine";
            String content = String.format(
                    "您好，\n\n" +
                    "感谢您对 AI Doc Engine 的关注和支持！\n\n" +
                    "您的反馈「%s」已处理完成。\n\n" +
                    "处理结果：\n%s\n\n" +
                    "反馈编号：%s\n\n" +
                    "再次感谢您的宝贵意见！\n\n" +
                    "AI Doc Engine 团队",
                    title,
                    reply,
                    feedbackId
            );
            
            sendSimpleEmail(email, subject, content);
            
            log.info("反馈处理完成邮件发送成功: email={}", email);
            
        } catch (Exception e) {
            log.warn("反馈处理完成邮件发送失败（非关键错误）", e);
        }
    }
    
    @Override
    public void sendFeedbackUpdateEmail(String email, String title, String updateContent, String feedbackId) {
        log.info("发送反馈更新通知邮件: email={}, feedbackId={}", email, feedbackId);
        
        try {
            String subject = "您的反馈推动了系统改进 - AI Doc Engine";
            String content = String.format(
                    "您好，\n\n" +
                    "感谢您的反馈！\n\n" +
                    "根据您的建议「%s」，我们对系统进行了以下改进：\n\n" +
                    "%s\n\n" +
                    "反馈编号：%s\n\n" +
                    "请体验新功能并继续支持我们！\n\n" +
                    "AI Doc Engine 团队",
                    title,
                    updateContent,
                    feedbackId
            );
            
            sendSimpleEmail(email, subject, content);
            
            log.info("反馈更新通知邮件发送成功: email={}", email);
            
        } catch (Exception e) {
            log.warn("反馈更新通知邮件发送失败（非关键错误）", e);
        }
    }
}
