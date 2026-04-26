package org.lin.fitnessworkout.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkoutPlanDTO {

    private Long id;
    private Long userId;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PlanItemDTO> items;

    @Data
    public static class PlanItemDTO {
        private Long id;
        private Integer dayOfWeek;
        private String exerciseType;
        private String exerciseTypeLabel;
        private Integer durationMinutes;
        private Integer caloriesBurned;
        private String status;
        private LocalDate scheduledDate;
    }
}
