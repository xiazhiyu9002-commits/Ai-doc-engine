package com.aidoc.engine.controller;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.common.response.ApiResponse;
import com.aidoc.engine.model.dto.formula.ValidateRequest;
import com.aidoc.engine.model.vo.formula.OcrResponse;
import com.aidoc.engine.model.vo.formula.ValidateResponse;
import com.aidoc.engine.service.FormulaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 公式控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/formula")
@RequiredArgsConstructor
public class FormulaController {
    
    private final FormulaService formulaService;
    
    /**
     * OCR 识别公式图片
     */
    @PostMapping("/ocr-image")
    public ApiResponse<OcrResponse> ocrImage(@RequestParam("image") MultipartFile image) {
        log.info("收到公式 OCR 请求: {}", image.getOriginalFilename());

        //暂时不用图片识别，服务器性能不够

        throw new BusinessException(ErrorCode.FORMULA_OCR_ERROR, "功能正在开发中......" );

//        OcrResponse response = formulaService.ocrImage(image);

//        return ApiResponse.success(response);
    }
    
    /**
     * 校验 LaTeX 公式格式
     */
    @PostMapping("/validate")
    public ApiResponse<ValidateResponse> validateLatex(@Valid @RequestBody ValidateRequest request) {
        log.info("收到公式校验请求: {}", request.getLatex());
        
        ValidateResponse response = formulaService.validateLatex(request.getLatex());
        
        return ApiResponse.success(response);
    }
}
