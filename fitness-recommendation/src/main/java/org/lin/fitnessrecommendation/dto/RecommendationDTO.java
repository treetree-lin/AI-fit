package org.lin.fitnessrecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDTO {

    private Long userId;

    /** 推荐来源：RULE / CF / HYBRID */
    private String source;

    /** 推荐列表 */
    private List<WorkoutRecommendItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkoutRecommendItem {
        private Long workoutId;
        private String title;
        private Double score;
        private String reason;
    }
}
