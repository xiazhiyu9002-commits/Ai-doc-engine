package com.aidoc.engine.service.impl;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.model.entity.LoginLogEntity;
import com.aidoc.engine.model.entity.UserEntity;
import com.aidoc.engine.model.vo.admin.AdminUserVO;
import com.aidoc.engine.model.vo.admin.DashboardTrendVO;
import com.aidoc.engine.model.vo.admin.DashboardVO;
import com.aidoc.engine.model.vo.admin.LoginLogVO;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.repository.AnnouncementRepository;
import com.aidoc.engine.repository.ExportTaskRepository;
import com.aidoc.engine.repository.FeedbackRepository;
import com.aidoc.engine.repository.FormulaOcrRecordRepository;
import com.aidoc.engine.repository.LoginLogRepository;
import com.aidoc.engine.repository.TemplateRepository;
import com.aidoc.engine.repository.UserRepository;
import com.aidoc.engine.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    
    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final TemplateRepository templateRepository;
    private final ExportTaskRepository exportTaskRepository;
    private final FormulaOcrRecordRepository formulaOcrRecordRepository;
    private final FeedbackRepository feedbackRepository;
    private final AnnouncementRepository announcementRepository;

    @Override
    public DashboardVO getDashboard() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        LocalDateTime weekAgoStart = todayStart.minusDays(7);
        LocalDateTime twoWeeksAgoStart = todayStart.minusDays(14);
        
        // 基础统计
        Long totalUsers = userRepository.count();
        Long activeUsers = userRepository.countByStatus("active");
        Long disabledUsers = userRepository.countByStatus("disabled");
        Long todayLogins = loginLogRepository.countByCreatedAtAfter(todayStart);
        Long totalLoginLogs = loginLogRepository.count();
        Long failedLoginLogs = loginLogRepository.countByLoginResult("failed");
        Long totalTemplates = templateRepository.count();
        
        // 新增统计
        Long totalExports = exportTaskRepository.count();
        Long ocrFailedCount = formulaOcrRecordRepository.countByStatus("FAILED");
        Long totalFeedbacks = feedbackRepository.count();
        Long totalAnnouncements = announcementRepository.count();
        
        // 计算环比增长率
        Long thisWeekUsers = userRepository.countByCreatedAtAfter(weekAgoStart);
        Long lastWeekUsers = userRepository.countByCreatedAtBetween(twoWeeksAgoStart, weekAgoStart);
        Double userGrowthRate = calculateGrowthRate(thisWeekUsers, lastWeekUsers);
        
        Long thisWeekExports = exportTaskRepository.countByStartTimeAfter(weekAgoStart);
        Long lastWeekExports = exportTaskRepository.countByStartTimeBetween(twoWeeksAgoStart, weekAgoStart);
        Double exportGrowthRate = calculateGrowthRate(thisWeekExports, lastWeekExports);
        
        Long thisWeekOcr = formulaOcrRecordRepository.countByCreatedAtAfter(weekAgoStart);
        Long lastWeekOcr = formulaOcrRecordRepository.countByCreatedAtBetween(twoWeeksAgoStart, weekAgoStart);
        Double ocrGrowthRate = calculateGrowthRate(thisWeekOcr, lastWeekOcr);
        
        Long thisWeekFeedbacks = feedbackRepository.countByCreatedAtAfter(weekAgoStart);
        Long lastWeekFeedbacks = feedbackRepository.countByCreatedAtAfter(twoWeeksAgoStart) - thisWeekFeedbacks;
        Double feedbackGrowthRate = calculateGrowthRate(thisWeekFeedbacks, lastWeekFeedbacks);
        
        return DashboardVO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .disabledUsers(disabledUsers)
                .todayLogins(todayLogins)
                .totalLoginLogs(totalLoginLogs)
                .failedLoginLogs(failedLoginLogs)
                .totalTemplates(totalTemplates)
                .totalExports(totalExports)
                .ocrFailedCount(ocrFailedCount)
                .totalFeedbacks(totalFeedbacks)
                .totalAnnouncements(totalAnnouncements)
                .userGrowthRate(userGrowthRate)
                .exportGrowthRate(exportGrowthRate)
                .ocrGrowthRate(ocrGrowthRate)
                .feedbackGrowthRate(feedbackGrowthRate)
                .build();
    }
    
    private Double calculateGrowthRate(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return Math.round((double)(current - previous) / previous * 10000.0) / 100.0;
    }

    @Override
    public DashboardTrendVO getTrend(String range) {
        if ("year".equals(range)) {
            return getMonthlyTrend();
        }
        return getDailyTrend(range);
    }
    
    private DashboardTrendVO getDailyTrend(String range) {
        int days = switch (range) {
            case "week" -> 7;
            case "month" -> 30;
            default -> 7;
        };
        
        List<String> dates = new ArrayList<>();
        List<Long> newUsers = new ArrayList<>();
        List<Long> activeUsers = new ArrayList<>();
        List<Long> exports = new ArrayList<>();
        List<Long> ocrCount = new ArrayList<>();
        List<Long> feedbacks = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            
            dates.add(date.format(formatter));
            newUsers.add(userRepository.countByCreatedAtBetween(dayStart, dayEnd));
            activeUsers.add(loginLogRepository.countDistinctUserIdByCreatedAtBetweenAndLoginResultSuccess(dayStart, dayEnd));
            exports.add(exportTaskRepository.countByStartTimeBetween(dayStart, dayEnd));
            ocrCount.add(formulaOcrRecordRepository.countByCreatedAtBetween(dayStart, dayEnd));
            feedbacks.add(feedbackRepository.countByCreatedAtAfter(dayStart) - feedbackRepository.countByCreatedAtAfter(dayEnd));
        }
        
        return DashboardTrendVO.builder()
                .dates(dates)
                .newUsers(newUsers)
                .activeUsers(activeUsers)
                .exports(exports)
                .ocrCount(ocrCount)
                .feedbacks(feedbacks)
                .build();
    }
    
    private DashboardTrendVO getMonthlyTrend() {
        List<String> dates = new ArrayList<>();
        List<Long> newUsers = new ArrayList<>();
        List<Long> activeUsers = new ArrayList<>();
        List<Long> exports = new ArrayList<>();
        List<Long> ocrCount = new ArrayList<>();
        List<Long> feedbacks = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (int i = 11; i >= 0; i--) {
            LocalDate firstDayOfMonth = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();
            LocalDateTime monthEnd = firstDayOfMonth.plusMonths(1).atStartOfDay();
            
            dates.add(firstDayOfMonth.format(formatter));
            newUsers.add(userRepository.countByCreatedAtBetween(monthStart, monthEnd));
            activeUsers.add(loginLogRepository.countDistinctUserIdByCreatedAtBetweenAndLoginResultSuccess(monthStart, monthEnd));
            exports.add(exportTaskRepository.countByStartTimeBetween(monthStart, monthEnd));
            ocrCount.add(formulaOcrRecordRepository.countByCreatedAtBetween(monthStart, monthEnd));
            feedbacks.add(feedbackRepository.countByCreatedAtAfter(monthStart) - feedbackRepository.countByCreatedAtAfter(monthEnd));
        }
        
        return DashboardTrendVO.builder()
                .dates(dates)
                .newUsers(newUsers)
                .activeUsers(activeUsers)
                .exports(exports)
                .ocrCount(ocrCount)
                .feedbacks(feedbacks)
                .build();
    }

    @Override
    public PageResult<AdminUserVO> getUsers(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<UserEntity> userPage;
        if (StringUtils.hasText(keyword)) {
            userPage = userRepository.findByKeyword(keyword.trim(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        
        List<AdminUserVO> items = userPage.getContent().stream()
                .map(this::buildAdminUserVO)
                .toList();
        
        return PageResult.<AdminUserVO>builder()
                .items(items)
                .total(userPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    @Override
    public AdminUserVO getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
        return buildAdminUserVO(user);
    }

    @Override
    @Transactional
    public AdminUserVO updateUserStatus(Long id, Integer status) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
        
        String newStatus = status == 1 ? "active" : "disabled";
        user.setStatus(newStatus);
        user = userRepository.save(user);
        
        log.info("管理员更新用户状态: userId={}, status={}", id, newStatus);
        return buildAdminUserVO(user);
    }

    @Override
    @Transactional
    public AdminUserVO updateUserRole(Long id, String role) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
        
        if (!role.equals("USER") && !role.equals("ADMIN")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效的角色类型");
        }
        
        user.setRole(role);
        user = userRepository.save(user);
        
        log.info("管理员更新用户角色: userId={}, role={}", id, role);
        return buildAdminUserVO(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
        
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能删除管理员账号");
        }
        
        userRepository.delete(user);
        
        log.info("管理员删除用户: userId={}", id);
    }

    @Override
    public PageResult<LoginLogVO> getLoginLogs(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<LoginLogEntity> logPage;
        if (StringUtils.hasText(keyword)) {
            logPage = loginLogRepository.findByKeyword(keyword.trim(), pageable);
        } else {
            logPage = loginLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        
        List<LoginLogVO> items = logPage.getContent().stream()
                .map(this::buildLoginLogVO)
                .toList();
        
        return PageResult.<LoginLogVO>builder()
                .items(items)
                .total(logPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    @Override
    public boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(user -> "ADMIN".equals(user.getRole()))
                .orElse(false);
    }

    private AdminUserVO buildAdminUserVO(UserEntity user) {
        return AdminUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .department(user.getDepartment())
                .role(user.getRole() != null ? user.getRole() : "USER")
                .status("active".equals(user.getStatus()) ? 1 : 0)
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private LoginLogVO buildLoginLogVO(LoginLogEntity entity) {
        String nickname = null;
        if (entity.getUserId() != null) {
            nickname = userRepository.findById(entity.getUserId())
                    .map(UserEntity::getNickname)
                    .orElse(null);
        }
        
        return LoginLogVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .usernameOrEmail(entity.getUsernameOrEmail())
                .nickname(nickname)
                .loginResult(entity.getLoginResult())
                .browserType(entity.getBrowserType())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .failReason(entity.getFailReason())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
