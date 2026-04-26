package org.lin.fitnessuser.dto;
import lombok.Data;

import java.util.List;

/**
 * @author lin
 * @date 2026-03-17
 */
@Data
public class RegisterRequest {
    private String username;
    private String password;
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
}

