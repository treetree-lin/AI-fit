package org.lin.fitnessrecommendation.service;

import org.lin.fitnessrecommendation.dto.RecommendationDTO;

public interface HybridRecommendationService {
    RecommendationDTO recommend(Long userId);
}
