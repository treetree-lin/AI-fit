package org.lin.fitnesscommon.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "age")
    private Integer age;

    @Convert(converter = org.lin.fitnesscommon.converter.GenderConverter.class)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "height")
    private Integer height;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "body_fat", precision = 4)
    private Double bodyFat;

    @Column(name = "goal", length = 50)
    private String goal;

    @Column(name = "level", length = 20)
    private String level;

    @Column(name = "avatar_url", length = 1000)
    protected String avatarUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "equipment")
    private List<String> equipment;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "injuries")
    private List<String> injuries;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "exp")
    private Integer exp = 0;

    @Column(name = "streak_days")
    private Integer streakDays = 0;

    @Column(name = "last_check_in_date")
    private LocalDate lastCheckInDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Gender {
        MALE("男"),
        FEMALE("女"),
        OTHER("其他");

        private final String chineseName;

        Gender(String chineseName) {
            this.chineseName = chineseName;
        }

        public String getChineseName() {
            return chineseName;
        }

        @JsonCreator
        public static Gender fromChineseName(String chineseName) {
            if (chineseName == null || chineseName.isEmpty()) {
                return null;
            }
            for (Gender gender : values()) {
                if (gender.getChineseName().equals(chineseName)) {
                    return gender;
                }
            }
            try {
                return Gender.valueOf(chineseName);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }
}
