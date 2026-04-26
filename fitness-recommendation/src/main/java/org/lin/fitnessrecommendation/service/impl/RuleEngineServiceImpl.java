package org.lin.fitnessrecommendation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.lin.fitnesscommon.entity.UserProfile;
import org.lin.fitnesscommon.entity.Workout;
import org.lin.fitnessrecommendation.service.RuleEngineService;
import org.lin.fitnessrecommendation.vo.ScoredWorkout;
import org.lin.fitnessuser.repository.UserProfileRepository;
import org.lin.fitnessworkout.repository.WorkoutRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleEngineServiceImpl implements RuleEngineService {

    private final UserProfileRepository userProfileRepository;
    private final WorkoutRepository workoutRepository;

    @Override
    public List<ScoredWorkout> recommend(Long userId, int topN) {
        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            log.warn("User profile not found for userId={}", userId);
            return Collections.emptyList();
        }
        UserProfile profile = profileOpt.get();
        List<Workout> workouts = workoutRepository.findByIsActiveTrue();

        String goal = profile.getGoal() != null ? profile.getGoal().toUpperCase() : "";
        String level = profile.getLevel() != null ? profile.getLevel().toUpperCase() : "";
        List<String> equipment = profile.getEquipment() != null ? profile.getEquipment() : Collections.emptyList();
        List<String> injuries = profile.getInjuries() != null ? profile.getInjuries() : Collections.emptyList();

        List<ScoredWorkout> scoredList = new ArrayList<>();
        for (Workout workout : workouts) {
            double score = 0.0;
            StringBuilder reason = new StringBuilder();

            // 1. 目标匹配
            if (goal.contains("增肌") || goal.contains("MUSCLE")) {
                if (workout.getTargetMuscle() != null && !workout.getTargetMuscle().isEmpty()) {
                    score += 3.0;
                    reason.append("符合增肌目标;");
                }
            } else if (goal.contains("减脂") || goal.contains("WEIGHT_LOSS")) {
                Integer cal = workout.getCaloriesBurned();
                if (cal != null) {
                    if (cal > 200) {
                        score += 3.0;
                        reason.append("高燃脂;");
                    } else if (cal > 100) {
                        score += 2.0;
                        reason.append("中等燃脂;");
                    }
                }
            } else if (goal.contains("耐力") || goal.contains("ENDURANCE")) {
                Integer dur = workout.getDurationMinutes();
                if (dur != null && dur > 30) {
                    score += 2.0;
                    reason.append("提升耐力;");
                }
            }

            // 2. 难度匹配
            if (level.contains("BEGINNER") || level.contains("新手")) {
                if (workout.getDifficulty() == Workout.Difficulty.BEGINNER) {
                    score += 3.0;
                    reason.append("新手友好;");
                }
            } else if (level.contains("INTERMEDIATE") || level.contains("中级")) {
                if (workout.getDifficulty() == Workout.Difficulty.INTERMEDIATE) {
                    score += 3.0;
                    reason.append("难度适中;");
                }
            } else if (level.contains("ADVANCED") || level.contains("高级")) {
                if (workout.getDifficulty() == Workout.Difficulty.ADVANCED) {
                    score += 3.0;
                    reason.append("高阶挑战;");
                }
            } else {
                score += 1.0; // 无明确等级给基础分
            }

            // 3. 器械匹配
            List<String> needed = workout.getEquipmentNeeded();
            if (needed != null && !needed.isEmpty() && !equipment.isEmpty()) {
                boolean hasEquip = needed.stream()
                        .allMatch(n -> equipment.stream()
                                .anyMatch(e -> e.equalsIgnoreCase(n) || n.toLowerCase().contains(e.toLowerCase())));
                if (hasEquip) {
                    score += 2.0;
                    reason.append("器械匹配;");
                }
            } else if (needed == null || needed.isEmpty()) {
                score += 1.0; // 无器械训练加分
                reason.append("无需器械;");
            }

            // 4. 伤病排除
            if (!injuries.isEmpty() && workout.getTargetMuscle() != null) {
                String target = workout.getTargetMuscle().toLowerCase();
                boolean conflict = injuries.stream()
                        .anyMatch(i -> target.contains(i.toLowerCase()));
                if (conflict) {
                    score = 0.0;
                    reason.setLength(0);
                    reason.append("与伤病部位冲突，已过滤");
                }
            }

            // 5. 热度加成
            Long favCount = workout.getFavoriteCount();
            if (favCount != null) {
                score += favCount / 100.0;
            }

            if (score > 0) {
                scoredList.add(new ScoredWorkout(
                        workout.getId(),
                        workout.getTitle(),
                        score,
                        reason.toString()
                ));
            }
        }

        scoredList.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return scoredList.stream().limit(topN).collect(Collectors.toList());
    }
}
