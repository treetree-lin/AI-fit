package org.lin.fitnesscommon.log.enums;

/**
 * @author lin
 * @date 2026-03-25
 */

public enum OperateType {

    INSERT("新增"),
    UPDATE("修改"),
    DELETE("删除"),
    SELECT("查询"),
    LOGIN("登录"),
    LOGOUT("登出"),
    UPLOAD("上传"),
    DOWNLOAD("下载"),
    OTHER("其他");

    private final String description;

    OperateType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}