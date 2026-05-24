package com.aidoc.engine.service;

import com.aidoc.engine.model.vo.formula.OcrResponse;
import com.aidoc.engine.model.vo.formula.ValidateResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 公式服务接口
 */
public interface FormulaService {
    
    /**
     * OCR 识别公式图片
     * 
     * @param image 公式图片
     * @return OCR 识别结果
     */
    OcrResponse ocrImage(MultipartFile image);
    
    /**
     * 校验 LaTeX 公式格式
     * 
     * @param latex LaTeX 公式
     * @return 校验结果
     */
    ValidateResponse validateLatex(String latex);
}
