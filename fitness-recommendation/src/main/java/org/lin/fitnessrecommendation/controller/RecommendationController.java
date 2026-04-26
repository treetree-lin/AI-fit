package org.lin.fitnessrecommendation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lin.fitnesscommon.vo.ApiResponse;
import org.lin.fitnessrecommendation.dto.RecommendationDTO;
import org.lin.fitnessrecommendation.service.CollaborativeFilteringService;
import org.lin.fitnessrecommendation.service.HybridRecommendationService;
import org.lin.fitnessrecommendation.service.RuleEngineService;
import org.lin.fitnessrecommendation.vo.ScoredWorkout;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecommendationController {

    private final RuleEngineService ruleEngineService;
    private final CollaborativeFilteringService collaborativeFilteringService;
    private final HybridRecommendationService hybridRecommendationService;

    /**
     * 混合推荐接口（默认）
     * GET /api/v1/recommendations?userId=1&topN=10
     */
    @GetMapping
    public ApiResponse<RecommendationDTO> getRecommendations(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int topN) {
        log.info("Get hybrid recommendations for userId={}, topN={}", userId, topN);
        try {
            RecommendationDTO result = hybridRecommendationService.recommend(userId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Hybrid recommendation failed", e);
            return ApiResponse.error("推荐失败：" + e.getMessage());
        }
    }

    /**
     * 规则引擎推荐
     * GET /api/v1/recommendations/rule?userId=1&topN=10
     */
    @GetMapping("/rule")
    public ApiResponse<List<ScoredWorkout>> getRuleRecommendations(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int topN) {
        log.info("Get rule-based recommendations for userId={}, topN={}", userId, topN);
        try {
            List<ScoredWorkout> result = ruleEngineService.recommend(userId, topN);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Rule-based recommendation failed", e);
            return ApiResponse.error("规则推荐失败：" + e.getMessage());
        }
    }

    /**
     * 协同过滤推荐
     * GET /api/v1/recommendations/cf?userId=1&topN=10
     */
    @GetMapping("/cf")
    public ApiResponse<List<ScoredWorkout>> getCfRecommendations(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int topN) {
        log.info("Get CF recommendations for userId={}, topN={}", userId, topN);
        try {
            List<ScoredWorkout> result = collaborativeFilteringService.recommend(userId, topN);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("CF recommendation failed", e);
            return ApiResponse.error("协同过滤推荐失败：" + e.getMessage());
        }
    }
}
