package org.lin.fitnessrecord.dto;

/**
 * @author lin
 * @date 2026-03-24
 */

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkoutRecordDTO {

    private Long id;
    private Long userId;
    private Long workoutId;
    private LocalDate recordDate;
    private Integer durationMinutes;
    private Integer caloriesBurned;
    private Boolean completed;
    private Integer rating;
    private String notes;
    private Integer stepCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<StepDTO> steps;

    @Data
    public static class StepDTO {
        private Long id;
        private Long stepId;
        private String exerciseName;
        private Integer actualSets;
        private String actualReps;
        private Double weightUsed;
        private Integer caloriesBurned;
    }
}

