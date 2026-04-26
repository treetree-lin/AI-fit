package org.lin.fitnessworkout.dto;

/**
 * @author lin
 * @date 2026-03-24
 */

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkoutDTO {

    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private Integer durationMinutes;
    private String targetMuscle;
    private List<String> equipmentNeeded;
    private Integer caloriesBurned;
    private Boolean isActive;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<StepDTO> steps;
    private String coverImageUrl;
    private String videoUrl;
    private Long favoriteCount;
    private Long commentCount;
    private Long viewCount;
    @Data
    public static class StepDTO {
        private Long id;
        private Integer stepOrder;
        private String exerciseName;
        private Integer sets;
        private String reps;
        private Integer restSeconds;
        private String tips;
        private String videoUrl;
        private Integer caloriesBurned;
    }
}
