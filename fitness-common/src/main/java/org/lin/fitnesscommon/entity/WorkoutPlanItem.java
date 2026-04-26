package org.lin.fitnesscommon.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "workout_plan_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", length = 30)
    private ExerciseType exerciseType;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "calories_burned")
    private Integer caloriesBurned;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ItemStatus status = ItemStatus.PENDING;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ItemStatus {
        PENDING, COMPLETED, SKIPPED
    }

    public enum ExerciseType {
        RUNNING("跑步"),
        JUMP_ROPE("跳绳"),
        STRENGTH("力量训练"),
        YOGA("瑜伽"),
        CYCLING("骑行"),
        SWIMMING("游泳"),
        HIIT("HIIT"),
        WALKING("步行"),
        OTHER("其他");

        private final String label;

        ExerciseType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
