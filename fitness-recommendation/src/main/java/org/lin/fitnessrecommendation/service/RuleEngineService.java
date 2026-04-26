package org.lin.fitnessrecommendation.service;

import org.lin.fitnessrecommendation.vo.ScoredWorkout;

import java.util.List;

public interface RuleEngineService {
    List<ScoredWorkout> recommend(Long userId, int topN);
}
