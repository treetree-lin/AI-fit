package org.lin.fitnessrecommendation.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoredWorkout {
    private Long workoutId;
    private String title;
    private Double score;
    private String reason;
}
