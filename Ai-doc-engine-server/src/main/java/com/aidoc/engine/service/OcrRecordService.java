package com.aidoc.engine.service;

import com.aidoc.engine.model.dto.ocr.OcrRecordQueryRequest;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.ocr.OcrRecordVO;
import com.aidoc.engine.model.vo.ocr.OcrStatsVO;

/**
 * OCR日志服务接口
 */
public interface OcrRecordService {
    
    /**
     * 分页查询OCR记录
     */
    PageResult<OcrRecordVO> getOcrRecords(OcrRecordQueryRequest request);
    
    /**
     * 根据ID获取OCR记录详情
     */
    OcrRecordVO getOcrRecordById(Long id);
    
    /**
     * 获取OCR统计信息
     */
    OcrStatsVO getOcrStats();
    
    /**
     * 获取指定用户的OCR记录
     */
    PageResult<OcrRecordVO> getOcrRecordsByUserId(Long userId, int page, int size);
    
    /**
     * 删除OCR记录
     */
    void deleteOcrRecord(Long id);
    
    /**
     * 批量删除OCR记录
     */
    void deleteOcrRecords(java.util.List<Long> ids);
}
