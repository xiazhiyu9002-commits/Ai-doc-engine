package com.aidoc.engine.service;

import com.aidoc.engine.model.dto.export.ExportTaskQueryRequest;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.export.ExportTaskStatsVO;
import com.aidoc.engine.model.vo.export.ExportTaskVO;

import java.util.List;

/**
 * 导出任务日志服务接口
 */
public interface ExportTaskService {
    
    /**
     * 分页查询导出任务
     */
    PageResult<ExportTaskVO> getExportTasks(ExportTaskQueryRequest request);
    
    /**
     * 根据ID获取导出任务详情
     */
    ExportTaskVO getExportTaskById(Long id);
    
    /**
     * 获取导出任务统计信息
     */
    ExportTaskStatsVO getExportTaskStats();
    
    /**
     * 获取指定用户的导出任务
     */
    PageResult<ExportTaskVO> getExportTasksByUserId(Long userId, int page, int size);
    
    /**
     * 获取指定模板的导出任务
     */
    PageResult<ExportTaskVO> getExportTasksByTemplateId(Long templateId, int page, int size);
    
    /**
     * 删除导出任务记录
     */
    void deleteExportTask(Long id);
    
    /**
     * 批量删除导出任务记录
     */
    void deleteExportTasks(List<Long> ids);
    
    /**
     * 重试失败的任务
     */
    void retryFailedTask(Long id);
}
