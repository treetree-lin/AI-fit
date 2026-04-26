package org.lin.fitnessrecommendation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lin.fitnesscommon.entity.Workout;
import org.lin.fitnesscommon.entity.WorkoutFavorite;
import org.lin.fitnesscommon.entity.WorkoutRecord;
import org.lin.fitnessrecommendation.service.CollaborativeFilteringService;
import org.lin.fitnessrecommendation.vo.ScoredWorkout;
import org.lin.fitnessrecord.repository.WorkoutRecordRepository;
import org.lin.fitnessworkout.repository.WorkoutFavoriteRepository;
import org.lin.fitnessworkout.repository.WorkoutRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollaborativeFilteringServiceImpl implements CollaborativeFilteringService {

    private final WorkoutRecordRepository workoutRecordRepository;
    private final WorkoutFavoriteRepository workoutFavoriteRepository;
    private final WorkoutRepository workoutRepository;

    @Override
    public List<ScoredWorkout> recommend(Long userId, int topN) {
        // 1. 构建用户-物品隐式评分矩阵
        List<WorkoutRecord> allRecords = workoutRecordRepository.findAll();
        List<WorkoutFavorite> allFavorites = workoutFavoriteRepository.findAll();

        Map<Long, Map<Long, Double>> userItemRatings = new HashMap<>();

        // 从 workout_records 提取评分
        for (WorkoutRecord record : allRecords) {
            if (record.getWorkoutId() == null) continue;
            userItemRatings
                    .computeIfAbsent(record.getUserId(), k -> new HashMap<>())
                    .merge(record.getWorkoutId(), computeImplicitRating(record), Double::sum);
        }

        // 叠加收藏信号
        for (WorkoutFavorite fav : allFavorites) {
            userItemRatings
                    .computeIfAbsent(fav.getUserId(), k -> new HashMap<>())
                    .merge(fav.getWorkoutId(), 2.0, Double::sum);
        }

        if (!userItemRatings.containsKey(userId)) {
            log.warn("No interaction history for userId={}", userId);
            return Collections.emptyList();
        }

        Map<Long, Double> targetUserRatings = userItemRatings.get(userId);

        // 2. 计算用户相似度（余弦相似度）
        Map<Long, Double> userSimilarities = new HashMap<>();
        double targetNorm = computeNorm(targetUserRatings);

        for (Map.Entry<Long, Map<Long, Double>> entry : userItemRatings.entrySet()) {
            Long otherUserId = entry.getKey();
            if (otherUserId.equals(userId)) continue;

            Map<Long, Double> otherRatings = entry.getValue();
            double dot = 0.0;
            for (Long itemId : targetUserRatings.keySet()) {
                if (otherRatings.containsKey(itemId)) {
                    dot += targetUserRatings.get(itemId) * otherRatings.get(itemId);
                }
            }
            double otherNorm = computeNorm(otherRatings);
            if (targetNorm > 0 && otherNorm > 0) {
                double sim = dot / (targetNorm * otherNorm);
                if (sim > 0) {
                    userSimilarities.put(otherUserId, sim);
                }
            }
        }

        // 3. 取 TopK 相似用户
        List<Map.Entry<Long, Double>> sortedSims = userSimilarities.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(10) // 固定 Top10 邻居
                .collect(Collectors.toList());

        if (sortedSims.isEmpty()) {
            log.warn("No similar users found for userId={}", userId);
            return Collections.emptyList();
        }

        // 4. 预测评分：对目标用户未交互的物品
        Set<Long> interactedItems = new HashSet<>(targetUserRatings.keySet());
        Map<Long, Double> predictedScores = new HashMap<>();
        Map<Long, Double> simSums = new HashMap<>();

        for (Map.Entry<Long, Double> simEntry : sortedSims) {
            Long neighborId = simEntry.getKey();
            double similarity = simEntry.getValue();
            Map<Long, Double> neighborRatings = userItemRatings.get(neighborId);

            for (Map.Entry<Long, Double> itemEntry : neighborRatings.entrySet()) {
                Long itemId = itemEntry.getKey();
                if (interactedItems.contains(itemId)) continue;

                predictedScores.merge(itemId, similarity * itemEntry.getValue(), Double::sum);
                simSums.merge(itemId, similarity, Double::sum);
            }
        }

        // 5. 归一化并排序
        List<ScoredWorkout> result = new ArrayList<>();
        Map<Long, Workout> workoutMap = workoutRepository.findByIsActiveTrue().stream()
                .collect(Collectors.toMap(Workout::getId, w -> w));

        for (Map.Entry<Long, Double> entry : predictedScores.entrySet()) {
            Long itemId = entry.getKey();
            double sumSim = simSums.getOrDefault(itemId, 0.0);
            if (sumSim <= 0) continue;
            double predScore = entry.getValue() / sumSim;

            Workout workout = workoutMap.get(itemId);
            if (workout == null || Boolean.FALSE.equals(workout.getIsActive())) continue;

            result.add(new ScoredWorkout(
                    itemId,
                    workout.getTitle(),
                    predScore,
                    "基于相似用户的协同过滤推荐"
            ));
        }

        result.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return result.stream().limit(topN).collect(Collectors.toList());
    }

    /**
     * 计算单条记录的隐式评分
     */
    private double computeImplicitRating(WorkoutRecord record) {
        double score = 0.0;
        if (Boolean.TRUE.equals(record.getCompleted())) {
            score += 3.0;
        } else {
            score += 1.0;
        }
        if (record.getRating() != null) {
            score += record.getRating(); // 1-5 分直接叠加
        }
        if (record.getDurationMinutes() != null) {
            score += Math.min(record.getDurationMinutes() / 10.0, 2.0);
        }
        return score;
    }

    private double computeNorm(Map<Long, Double> ratings) {
        double sum = 0.0;
        for (double v : ratings.values()) {
            sum += v * v;
        }
        return Math.sqrt(sum);
    }
}
