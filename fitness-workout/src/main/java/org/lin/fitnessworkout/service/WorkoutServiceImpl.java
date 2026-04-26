package org.lin.fitnessworkout.service;
import org.lin.fitnesscommon.constant.RedisConstants;
import org.lin.fitnesscommon.utils.RedisUtil;
import org.lin.fitnessworkout.dto.WorkoutDTO;
import org.lin.fitnesscommon.entity.Workout;
import org.lin.fitnesscommon.entity.WorkoutStep;
import org.lin.fitnessworkout.repository.WorkoutRepository;
import org.lin.fitnessworkout.repository.WorkoutStepRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lin
 * @date 2026-03-24
 */

@Service
public class WorkoutServiceImpl implements WorkoutService {
Logger logger = LoggerFactory.getLogger(WorkoutServiceImpl.class);
    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private WorkoutStepRepository workoutStepRepository;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Transactional
    public WorkoutDTO createWorkout(WorkoutDTO request, Long adminId) {
        Workout workout = new Workout();
        BeanUtils.copyProperties(request, workout);
        workout.setCreatedBy(adminId);
        workout.setIsActive(true);
        
        // 显式设置统计字段默认值，防止 BeanUtils 覆盖为 null
        if (workout.getFavoriteCount() == null) {
            workout.setFavoriteCount(0L);
        }
        if (workout.getCommentCount() == null) {
            workout.setCommentCount(0L);
        }
        if (workout.getViewCount() == null) {
            workout.setViewCount(0L);
        }

        if (request.getSteps() != null && !request.getSteps().isEmpty()) {
            List<WorkoutStep> steps = request.getSteps().stream().map(stepDTO -> {
                WorkoutStep step = new WorkoutStep();
                BeanUtils.copyProperties(stepDTO, step);
                step.setWorkoutId(null);
                return step;
            }).collect(Collectors.toList());

            for (int i = 0; i < steps.size(); i++) {
                steps.get(i).setStepOrder(i + 1);
            }
            workout.setSteps(steps);
        }

        Workout saved = workoutRepository.save(workout);
        return convertToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutDTO> getAllActiveWorkouts() {
        return workoutRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutDTO getWorkoutById(Long id) {
        // 先从缓存获取统计数据
        Map<Object, Object> statsCache = (Map<Object, Object>) redisUtil.getHash(
                RedisConstants.WORKOUT_STATS_KEY_PREFIX + id
        );

        WorkoutDTO dto = convertToDTO(
                workoutRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("健身方案不存在"))
        );

        // 如果缓存存在，直接使用
        if (statsCache != null && !statsCache.isEmpty()) {
            dto.setFavoriteCount(((Number) statsCache.get("favoriteCount")).longValue());
            dto.setCommentCount(((Number) statsCache.get("commentCount")).longValue());
            dto.setViewCount(((Number) statsCache.get("viewCount")).longValue());
        } else {
            // 缓存不存在，从数据库加载并写入缓存
            Workout workout = workoutRepository.findById(id).orElse(null);
            if (workout != null) {
                Map<String, Object> statsMap = new HashMap<>();
                statsMap.put("favoriteCount", workout.getFavoriteCount());
                statsMap.put("commentCount", workout.getCommentCount());
                statsMap.put("viewCount", workout.getViewCount());

                redisUtil.setHash(
                        RedisConstants.WORKOUT_STATS_KEY_PREFIX + id,
                        statsMap,
                        RedisConstants.STATS_CACHE_EXPIRE_DAYS,
                        TimeUnit.DAYS
                );

                dto.setFavoriteCount(workout.getFavoriteCount());
                dto.setCommentCount(workout.getCommentCount());
                dto.setViewCount(workout.getViewCount());
            }
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutDTO> getByDifficulty(String difficulty) {
        Workout.Difficulty diff = Workout.Difficulty.valueOf(difficulty.toUpperCase());
        return workoutRepository.findByDifficultyAndIsActiveTrue(diff).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutDTO> getByTargetMuscle(String targetMuscle) {
        return workoutRepository.findByTargetMuscleContainingIgnoreCaseAndIsActiveTrue(targetMuscle).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkoutDTO updateWorkout(Long id, WorkoutDTO request) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("健身方案不存在"));

        BeanUtils.copyProperties(request, workout, "id", "createdBy", "createdAt", "updatedAt");

        if (request.getSteps() != null) {
            workoutStepRepository.deleteByWorkoutId(id);
            List<WorkoutStep> steps = request.getSteps().stream().map(stepDTO -> {
                WorkoutStep step = new WorkoutStep();
                BeanUtils.copyProperties(stepDTO, step);
                step.setWorkoutId(id);
                return step;
            }).collect(Collectors.toList());

            for (int i = 0; i < steps.size(); i++) {
                steps.get(i).setStepOrder(i + 1);
            }
            workout.setSteps(steps);
        }

        Workout updated = workoutRepository.save(workout);
        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteWorkout(Long id) {
        if (!workoutRepository.existsById(id)) {
            throw new RuntimeException("健身方案不存在");
        }
        workoutRepository.deleteById(id);
    }

    @Override
    @Transactional
    public WorkoutDTO toggleWorkoutStatus(Long id) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("健身方案不存在"));
        workout.setIsActive(!workout.getIsActive());
        Workout updated = workoutRepository.save(workout);
        return convertToDTO(updated);
    }

    private WorkoutDTO convertToDTO(Workout workout) {
        WorkoutDTO dto = new WorkoutDTO();
        BeanUtils.copyProperties(workout, dto);
        if (workout.getSteps() != null) {
            dto.setSteps(workout.getSteps().stream()
                    .map(step -> {
                        WorkoutDTO.StepDTO stepDTO = new WorkoutDTO.StepDTO();
                        BeanUtils.copyProperties(step, stepDTO);
                        return stepDTO;
                    })
                    .collect(Collectors.toList()));
        }
        return dto;
    }
    @Override
    @Transactional
    public void incrementFavoriteCount(Long workoutId) {
        // 更新数据库
        workoutRepository.incrementFavoriteCount(workoutId);

        // 更新 Redis 缓存
        redisUtil.incrementHash(
                RedisConstants.WORKOUT_STATS_KEY_PREFIX + workoutId,
                "favoriteCount",
                1
        );

        logger.info("教程{}收藏数 +1", workoutId);
    }

    @Override
    @Transactional
    public void decrementFavoriteCount(Long workoutId) {
        workoutRepository.decrementFavoriteCount(workoutId);

        redisUtil.incrementHash(
                RedisConstants.WORKOUT_STATS_KEY_PREFIX + workoutId,
                "favoriteCount",
                -1
        );

        logger.info("教程{}收藏数 -1", workoutId);
    }

    @Override
    @Transactional
    public void incrementCommentCount(Long workoutId) {
        workoutRepository.incrementCommentCount(workoutId);

        redisUtil.incrementHash(
                RedisConstants.WORKOUT_STATS_KEY_PREFIX + workoutId,
                "commentCount",
                1
        );

        logger.info("教程{}评论数 +1", workoutId);
    }

    @Override
    @Transactional
    public void decrementCommentCount(Long workoutId) {
        workoutRepository.decrementCommentCount(workoutId);

        redisUtil.incrementHash(
                RedisConstants.WORKOUT_STATS_KEY_PREFIX + workoutId,
                "commentCount",
                -1
        );

        logger.info("教程{}评论数 -1", workoutId);
    }

    @Override
    @Transactional
    public void incrementViewCount(Long workoutId) {
        // 浏览数每次都更新 Redis，定期同步到数据库
        Long newCount = redisUtil.incrementHash(
                RedisConstants.WORKOUT_STATS_KEY_PREFIX + workoutId,
                "viewCount",
                1
        );

        // 每 100 次浏览同步一次到数据库（可配置）
        if (newCount % 100 == 0) {
            workoutRepository.updateViewCount(workoutId, newCount);
        }

        logger.info("教程{}浏览数 +1，当前：{}", workoutId, newCount);
    }

// ... existing code ...
}

