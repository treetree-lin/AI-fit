package org.lin.fitnessworkout.mq.event;

/**
 * @author lin
 * @date 2026-03-25
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lin.fitnesscommon.config.MdcUtil;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteEvent {
    private String traceId;
    private Long userId;
    private Long workoutId;
    private String action;
    private LocalDateTime timestamp;

    public FavoriteEvent(Long userId, Long workoutId, String action, LocalDateTime timestamp) {
        this.userId = userId;
        this.workoutId = workoutId;
        this.action = action;
        this.timestamp = timestamp;
        // 自动获取当前 MDC 中的 TraceID
        this.traceId = MdcUtil.getTraceId();
    }

}
