package com.aidoc.engine.model.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTrendVO {
    
    private List<String> dates;
    private List<Long> newUsers;
    private List<Long> activeUsers;
    private List<Long> exports;
    private List<Long> ocrCount;
    private List<Long> feedbacks;
}
