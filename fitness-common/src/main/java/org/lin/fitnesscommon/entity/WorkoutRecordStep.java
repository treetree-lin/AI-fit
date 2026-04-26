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
@Table(name = "workout_record_steps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutRecordStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "step_id")
    private Long stepId;

    @Column(name = "exercise_name", nullable = false, length = 255)
    private String exerciseName;

    @Column(name = "actual_sets", nullable = false)
    private Integer actualSets;

    @Column(name = "actual_reps", length = 50)
    private String actualReps;

    @Column(name = "weight_used", precision = 5)
    private Double weightUsed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
