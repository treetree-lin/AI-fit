package org.lin.fitnesscommon.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lin
 * @date 2026-03-24
 */

@Entity
@Table(name = "workouts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    private Difficulty difficulty = Difficulty.BEGINNER;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "target_muscle", length = 100)
    private String targetMuscle;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "equipment_needed")
    private List<String> equipmentNeeded;

    @Column(name = "calories_burned")
    private Integer caloriesBurned;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;


    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "video_url", length = 500)
    private String videoUrl;


    @Column(name = "favorite_count", nullable = false)
    private Long favoriteCount = 0L;

    @Column(name = "comment_count", nullable = false)
    private Long commentCount = 0L;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "workoutId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkoutStep> steps;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Difficulty {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}
