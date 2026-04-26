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
@Table(name = "workout_comment_likes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "comment_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutCommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
