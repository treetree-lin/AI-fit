package org.lin.fitnesscommon.log.model;

/**
 * @author lin
 * @date 2026-03-25
 */

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LogRecord {

    /**
     * 日志ID
     */
    private String traceId;
    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作类型
     */
    private String operateType;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 请求参数
     */
    private String params;

    /**
     * 返回结果
     */
    private String result;

    /**
     * 执行耗时(ms)
     */
    private Long costTime;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMsg;



}