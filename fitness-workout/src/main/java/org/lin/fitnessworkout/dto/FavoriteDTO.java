package org.lin.fitnessworkout.dto;

/**
 * @author lin
 * @date 2026-03-25
 */

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FavoriteDTO {

    private Long id;
    private Long userId;
    private Long workoutId;
    private LocalDateTime createdAt;
}
