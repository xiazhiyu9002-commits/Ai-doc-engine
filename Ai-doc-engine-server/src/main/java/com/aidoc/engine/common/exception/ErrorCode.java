package com.aidoc.engine.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用错误码 (400-499)
    BAD_REQUEST(400, "请求参数错误"),
    INVALID_PARAMETER(400, "参数无效"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户认证错误 (1000-1099)
    USERNAME_EXISTS(1001, "用户名已存在"),
    EMAIL_EXISTS(1002, "邮箱已存在"),
    INVALID_CREDENTIALS(1003, "用户名或密码错误"),
    EMAIL_NOT_FOUND(1004, "邮箱不存在"),
    INVALID_TOKEN(1005, "令牌无效或已过期"),
    USER_DISABLED(1006, "用户已被禁用"),
    USER_NOT_FOUND(1007, "用户不存在"),

    // 文档处理错误 (2000-2099)
    MARKDOWN_PARSE_ERROR(2001, "Markdown解析失败"),
    UDM_FORMAT_ERROR(2002, "UDM格式错误"),
    TEMPLATE_NOT_FOUND(2003, "模板不存在"),
    DOCUMENT_EXPORT_ERROR(2004, "文档导出失败"),
    DOCUMENT_NOT_FOUND(2005, "文档不存在"),
    NORMALIZER_ERROR(2006, "文档规范化失败"),
    EXPORT_FAILED(2007, "导出失败"),
    PARSE_FAILED(2008, "解析失败"),
    TEMPLATE_NAME_EXISTS(2009, "模板名称已存在"),
    TEMPLATE_VERSION_NOT_FOUND(2010, "模板版本不存在"),

    // 公式处理错误 (3000-3099)
    FORMULA_OCR_ERROR(3001, "公式识别失败"),
    LATEX_FORMAT_ERROR(3002, "LaTeX格式错误"),
    MATHML_CONVERT_ERROR(3003, "MathML转换失败"),
    OMML_CONVERT_ERROR(3004, "OMML转换失败"),
    FORMULA_VALIDATE_ERROR(3005, "公式校验失败"),
    OCR_SERVICE_ERROR(3006, "OCR服务调用失败"),
    OCR_RECORD_NOT_FOUND(3007, "OCR记录不存在"),

    // 流程图处理错误 (4000-4099)
    FLOWCHART_JSON_ERROR(4001, "流程图JSON格式错误"),
    DRAWINGML_CONVERT_ERROR(4002, "DrawingML转换失败"),
    FLOWCHART_NODE_ERROR(4003, "流程图节点错误"),
    FLOWCHART_EDGE_ERROR(4004, "流程图边错误"),

    // 文件处理错误 (5000-5099)
    FILE_UPLOAD_ERROR(5001, "文件上传失败"),
    FILE_SIZE_EXCEEDED(5002, "文件大小超过限制"),
    FILE_TYPE_NOT_ALLOWED(5003, "文件类型不允许"),
    FILE_NOT_FOUND(5004, "文件不存在"),
    FILE_STORAGE_ERROR(5005, "文件存储失败"),

    // 邮件发送错误 (6000-6099)
    EMAIL_SEND_ERROR(6001, "邮件发送失败"),
    EMAIL_TEMPLATE_ERROR(6002, "邮件模板错误"),

    // 管理后台错误 (7000-7099)
    ADMIN_ACCESS_DENIED(7001, "无管理员权限"),
    ADMIN_USER_PROTECTED(7002, "受保护的管理员账号不可操作"),

    // 反馈错误 (8000-8099)
    FEEDBACK_NOT_FOUND(8001, "反馈不存在"),
    FEEDBACK_ACCESS_DENIED(8002, "无权访问此反馈"),
    FEEDBACK_ALREADY_CLOSED(8003, "反馈已关闭"),
    FEEDBACK_IMAGE_LIMIT_EXCEEDED(8004, "图片数量超过限制"),
    FEEDBACK_CONTENT_TOO_LONG(8005, "反馈内容过长"),

    // 公告错误 (8100-8199)
    ANNOUNCEMENT_NOT_FOUND(8101, "公告不存在"),
    ANNOUNCEMENT_ALREADY_PUBLISHED(8102, "公告已发布"),

    // 导出任务错误 (8200-8299)
    EXPORT_TASK_NOT_FOUND(8201, "导出任务不存在"),
    EXPORT_TASK_CANNOT_RETRY(8202, "只能重试失败的任务"),

    // 密码重置错误 (8300-8399)
    PASSWORD_RESET_TOKEN_NOT_FOUND(8301, "密码重置令牌不存在"),
    PASSWORD_RESET_TOKEN_EXPIRED(8302, "密码重置令牌已过期"),
    PASSWORD_RESET_TOKEN_USED(8303, "密码重置令牌已使用"),

    // 用户个人中心错误 (9000-9099)
    BINDING_NOT_FOUND(9001, "绑定不存在"),
    BINDING_ACCESS_DENIED(9002, "无权操作此绑定"),
    PASSWORD_SAME_AS_OLD(9003, "新密码不能与旧密码相同");

    private final Integer code;
    private final String message;
}
