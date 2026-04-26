package org.lin.fitnessworkout.mq.event;

/**
 * @author lin
 * @date 2026-03-25
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentEvent {

    private Long userId;
    private Long workoutId;
    private Long commentId;
    private String action;
    private LocalDateTime timestamp;
}
