package org.lin.fitnesscommon.entity;

/**
 * @author lin
 * @date 2026-03-25
 */

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "workout_favorites",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "workout_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "workout_id", nullable = false)
    private Long workoutId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
