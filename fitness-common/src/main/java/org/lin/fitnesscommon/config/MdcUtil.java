package org.lin.fitnesscommon.config;

/**
 * @author lin
 * @date 2026-03-27
 */

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import java.util.UUID;

@Slf4j
public class MdcUtil {

    private static final String TRACE_ID = "traceId";
    private static final int TRACE_ID_LENGTH = 32;

    /**
     * 生成并设置 TraceID
     */
    public static void setTraceId() {
        String traceId = generateTraceId();
        MDC.put(TRACE_ID, traceId);
        log.debug("设置 TraceID: {}", traceId);
    }

    /**
     * 手动设置 TraceID（用于从消息队列或 HTTP 头中获取）
     */
    public static void setTraceId(String traceId) {
        if (traceId == null || traceId.trim().isEmpty()) {
            setTraceId();
        } else {
            MDC.put(TRACE_ID, traceId);
            log.debug("使用已有 TraceID: {}", traceId);
        }
    }

    /**
     * 获取当前 TraceID
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    /**
     * 清除 TraceID
     */
    public static void clear() {
        MDC.clear();
        log.debug("清除 TraceID");
    }

    /**
     * 生成 TraceID（32 位 UUID，去掉横杠）
     */
    private static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

    /**
     * 执行带 TraceID 的操作（自动清理）
     */
    public static void executeWithTraceId(Runnable action) {
        try {
            if (MDC.get(TRACE_ID) == null) {
                setTraceId();
            }
            action.run();
        } finally {
            clear();
        }
    }
}
