package org.lin.fitnessrecommendation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lin.fitnessrecommendation.config.RecommendationProperties;
import org.lin.fitnessrecommendation.dto.RecommendationDTO;
import org.lin.fitnessrecommendation.service.CollaborativeFilteringService;
import org.lin.fitnessrecommendation.service.HybridRecommendationService;
import org.lin.fitnessrecommendation.service.RuleEngineService;
import org.lin.fitnessrecommendation.vo.ScoredWorkout;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HybridRecommendationServiceImpl implements HybridRecommendationService {

    private final RuleEngineService ruleEngineService;
    private final CollaborativeFilteringService collaborativeFilteringService;
    private final RecommendationProperties properties;

    @Override
    public RecommendationDTO recommend(Long userId) {
        int maxN = properties.getMaxRecommendations();

        List<ScoredWorkout> ruleResults = ruleEngineService.recommend(userId, maxN);
        List<ScoredWorkout> cfResults = collaborativeFilteringService.recommend(userId, maxN);

        double ruleWeight = properties.getRuleWeight();
        double cfWeight = properties.getCfWeight();

        // 归一化
        double maxRuleScore = ruleResults.stream().mapToDouble(ScoredWorkout::getScore).max().orElse(1.0);
        double maxCfScore = cfResults.stream().mapToDouble(ScoredWorkout::getScore).max().orElse(1.0);
        if (maxRuleScore <= 0) maxRuleScore = 1.0;
        if (maxCfScore <= 0) maxCfScore = 1.0;

        Map<Long, Double> mergedScores = new HashMap<>();
        Map<Long, StringBuilder> mergedReasons = new HashMap<>();

        for (ScoredWorkout sw : ruleResults) {
            double norm = sw.getScore() / maxRuleScore;
            mergedScores.merge(sw.getWorkoutId(), ruleWeight * norm, Double::sum);
            mergedReasons.computeIfAbsent(sw.getWorkoutId(), k -> new StringBuilder())
                    .append("规则引擎:").append(String.format("%.2f", norm)).append(";");
        }

        for (ScoredWorkout sw : cfResults) {
            double norm = sw.getScore() / maxCfScore;
            mergedScores.merge(sw.getWorkoutId(), cfWeight * norm, Double::sum);
            mergedReasons.computeIfAbsent(sw.getWorkoutId(), k -> new StringBuilder())
                    .append("协同过滤:").append(String.format("%.2f", norm)).append(";");
        }

        // 排序取 TopN
        List<Map.Entry<Long, Double>> sorted = mergedScores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(maxN)
                .collect(Collectors.toList());

        List<RecommendationDTO.WorkoutRecommendItem> items = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : sorted) {
            Long workoutId = entry.getKey();
            // 查找标题（优先从 ruleResults/cfResults 中复用）
            String title = findTitle(workoutId, ruleResults, cfResults);
            items.add(new RecommendationDTO.WorkoutRecommendItem(
                    workoutId,
                    title,
                    entry.getValue(),
                    mergedReasons.getOrDefault(workoutId, new StringBuilder("混合推荐")).toString()
            ));
        }

        log.info("Hybrid recommend for userId={}, ruleSize={}, cfSize={}, mergedSize={}",
                userId, ruleResults.size(), cfResults.size(), items.size());

        return new RecommendationDTO(userId, "HYBRID", items);
    }

    private String findTitle(Long workoutId, List<ScoredWorkout> ruleResults, List<ScoredWorkout> cfResults) {
        for (ScoredWorkout sw : ruleResults) {
            if (sw.getWorkoutId().equals(workoutId)) return sw.getTitle();
        }
        for (ScoredWorkout sw : cfResults) {
            if (sw.getWorkoutId().equals(workoutId)) return sw.getTitle();
        }
        return "";
    }
}
