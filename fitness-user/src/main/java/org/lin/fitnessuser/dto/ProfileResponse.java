package org.lin.fitnessuser.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lin
 * @date 2026-03-17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Long userId;
    private String username;
    private Integer age;
    private String gender;
    private Double height;
    private Double weight;
    private Double bodyFat;
    private String goal;
    private String level;
    private List<String> equipment;
    private List<String> injuryHistory;
    private String avatarUrl;
    private String bio;
    private String location;
    private Integer exp;
    private Integer streakDays;
    private Integer totalWorkouts;
    private Integer totalMinutes;
    private Integer totalCalories;
    
    // 管理员专属字段
    private String role;  // 用户角色：USER 或 ADMIN
    private Boolean isAdmin;  // 是否为管理员
    private LocalDateTime createdAt;  // 账号创建时间（仅管理员可见）
    private Boolean profileCompleted;  // 资料是否完善（仅管理员可见）
}
