package org.lin.fitnesscommon.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
// import java.util.List;
/**
 * @author lin
 * @date 2026-03-24
 */



@Entity
@Table(name = "workout_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "workout_id")
    private Long workoutId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "calories_burned")
    private Integer caloriesBurned;

    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "step_count")
    private Integer stepCount;

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
}

