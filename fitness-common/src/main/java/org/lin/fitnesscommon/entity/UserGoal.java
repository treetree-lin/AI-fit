package org.lin.fitnesscommon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author lin
 * @date 2026-03-24
 */
@Entity
@Table(name = "user_goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "goal_type", nullable = false, length = 20)
    private String goalType;

    @Column(name = "target_value", nullable = false)
    private Integer targetValue;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", nullable = false)
    private Integer status = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum GoalType {
        WEEKLY_FREQUENCY, WEIGHT_LOSS, MUSCLE_GAIN, ENDURANCE
    }

    public enum Status {
        ONGOING(0), COMPLETED(1), ABANDONED(2);

        private final int value;
        Status(int value) {
            this.value = value;
        }
    }
}

