package org.lin.fitnesscommon.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
/**
 * @author lin
 * @date 2026-03-24
 */



@Entity
@Table(name = "workout_steps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workout_id", nullable = false)
    private Long workoutId;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "exercise_name", nullable = false, length = 255)
    private String exerciseName;

    @Column(name = "sets", nullable = false)
    private Integer sets;

    @Column(name = "reps", length = 50)
    private String reps;

    @Column(name = "rest_seconds")
    private Integer restSeconds;

    @Column(name = "tips", columnDefinition = "TEXT")
    private String tips;

    @Column(name = "video_url", length = 255)
    private String videoUrl;

    @Column(name = "calories_burned")
    private Integer caloriesBurned;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
