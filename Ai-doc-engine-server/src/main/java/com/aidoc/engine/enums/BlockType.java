package com.aidoc.engine.enums;

/**
 * UDM Block 类型枚举
 */
public enum BlockType {
    HEADING("heading", "标题"),
    PARAGRAPH("paragraph", "段落"),
    LIST("list", "列表"),
    TABLE("table", "表格"),
    FORMULA("formula", "公式"),
    FLOWCHART("flowchart", "流程图"),
    IMAGE("image", "图片"),
    CODE("code", "代码块"),
    CODEBLOCK("codeblock", "代码块"),  // 兼容 codeblock 类型
    TASKLIST("tasklist", "任务列表"),
    FOOTNOTE("footnote", "脚注");

    private final String code;
    private final String description;

    BlockType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static BlockType fromCode(String code) {
        for (BlockType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown block type: " + code);
    }
}
