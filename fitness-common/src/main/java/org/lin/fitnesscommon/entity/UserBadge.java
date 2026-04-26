package org.lin.fitnesscommon.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "badge_code", nullable = false, length = 50)
    private String badgeCode;

    @Column(name = "badge_name", nullable = false, length = 100)
    private String badgeName;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "earned", nullable = false)
    private Boolean earned = false;

    @Column(name = "earned_at")
    private LocalDateTime earnedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
