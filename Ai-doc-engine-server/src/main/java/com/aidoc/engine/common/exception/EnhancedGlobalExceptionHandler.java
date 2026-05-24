package com.aidoc.engine.common.exception;

import com.aidoc.engine.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class EnhancedGlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.info("业务异常: code={}, message={}", ex.getCode(), ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ex.getCode(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("参数验证失败: {}", errors);

        ApiResponse<Map<String, String>> response = ApiResponse.error(
            ErrorCode.BAD_REQUEST.getCode(),
            "参数验证失败",
            errors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        
        String violations = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        log.warn("约束违反: {}", violations);

        ApiResponse<String> response = ApiResponse.error(
            ErrorCode.BAD_REQUEST.getCode(),
            "参数验证失败: " + violations,
            violations
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        
        log.warn("请求体解析失败: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.BAD_REQUEST.getCode(),
            "请求体格式错误，请检查JSON格式"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotWritableException(
            HttpMessageNotWritableException ex) {
        
        log.error("响应体序列化失败: ", ex);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INTERNAL_ERROR.getCode(),
            "服务器异常，请稍后重试"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        
        String message = String.format("参数 '%s' 类型错误，期望类型: %s", 
            ex.getName(), 
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "未知");

        log.warn("参数类型错误: {}", message);

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        log.warn("非法参数: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(
            BadCredentialsException ex) {
        
        log.warn("认证失败: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INVALID_CREDENTIALS.getCode(),
            ErrorCode.INVALID_CREDENTIALS.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceAccessException(
            ResourceAccessException ex) {
        
        log.error("外部服务连接失败: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INTERNAL_ERROR.getCode(),
            "服务器异常，请稍后重试！"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiResponse<Void>> handleRestClientException(
            RestClientException ex) {
        
        log.error("REST客户端调用失败: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INTERNAL_ERROR.getCode(),
            "服务器异常，请稍后重试！"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("系统异常: ", ex);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INTERNAL_ERROR.getCode(),
            "系统内部错误，请稍后重试"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Void>> handleNullPointerException(NullPointerException ex) {
        log.error("空指针异常: ", ex);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INTERNAL_ERROR.getCode(),
            "系统处理异常，请联系管理员"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(OutOfMemoryError.class)
    public ResponseEntity<ApiResponse<Void>> handleOutOfMemoryError(OutOfMemoryError ex) {
        log.error("内存溢出: ", ex);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INTERNAL_ERROR.getCode(),
            "系统资源不足，请稍后重试或减小文档大小"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
