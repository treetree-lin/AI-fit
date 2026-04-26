package org.lin.fitnessworkout.service;

/**
 * @author lin
 * @date 2026-03-25
 */

import lombok.extern.slf4j.Slf4j;
import org.lin.fitnesscommon.constant.RedisConstants;
import org.lin.fitnesscommon.utils.RedisUtil;
import org.lin.fitnessworkout.dto.FavoriteDTO;
import org.lin.fitnesscommon.entity.Workout;
import org.lin.fitnesscommon.entity.WorkoutFavorite;
import org.lin.fitnessworkout.mq.event.FavoriteEvent;
import org.lin.fitnessworkout.mq.producer.StatsEventProducer;
import org.lin.fitnessworkout.repository.WorkoutFavoriteRepository;
import org.lin.fitnessworkout.repository.WorkoutRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private WorkoutFavoriteRepository favoriteRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private StatsEventProducer statsEventProducer;

    @Override
    @Transactional
    public FavoriteDTO addFavorite(Long userId, Long workoutId) {
        String favoriteKey = RedisConstants.USER_FAVORITE_KEY_PREFIX + userId + ":" + workoutId;

        Boolean isFavorited = redisUtil.getBooleanValue(favoriteKey);

        if (Boolean.TRUE.equals(isFavorited)) {
            throw new RuntimeException("已经收藏过了");
        }

        if (favoriteRepository.existsByUserIdAndWorkoutId(userId, workoutId)) {
            throw new RuntimeException("已经收藏过了");
        }

        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("健身方案不存在"));

        WorkoutFavorite favorite = new WorkoutFavorite();
        favorite.setUserId(userId);
        favorite.setWorkoutId(workoutId);
        favoriteRepository.save(favorite);

        redisUtil.setBooleanValue(
            favoriteKey,
            true,
            RedisConstants.FAVORITE_CACHE_EXPIRE_DAYS,
            TimeUnit.DAYS
        );

        FavoriteEvent event = new FavoriteEvent(userId, workoutId, "ADD", LocalDateTime.now());
        statsEventProducer.sendFavoriteEvent(event);

//        Map<String, Object> statsMap = new HashMap<>();
//        statsMap.put("favoriteCount", workout.getFavoriteCount() + 1);
//        statsMap.put("commentCount", workout.getCommentCount());
//        statsMap.put("viewCount", workout.getViewCount());
//
//        redisUtil.setHash(
//            RedisConstants.WORKOUT_STATS_KEY_PREFIX + workoutId,
//            statsMap,
//            RedisConstants.STATS_CACHE_EXPIRE_DAYS,
//            TimeUnit.DAYS
//        );

        log.info("用户{}收藏教程{}", userId, workoutId);

        FavoriteDTO dto = new FavoriteDTO();
        BeanUtils.copyProperties(favorite, dto);
        return dto;
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long workoutId) {
        String favoriteKey = RedisConstants.USER_FAVORITE_KEY_PREFIX + userId + ":" + workoutId;

        Boolean isFavorited = redisUtil.getBooleanValue(favoriteKey);

        if (Boolean.FALSE.equals(isFavorited)) {
            if (!favoriteRepository.existsByUserIdAndWorkoutId(userId, workoutId)) {
                throw new RuntimeException("还未收藏该教程");
            }
        }

        favoriteRepository.deleteByUserIdAndWorkoutId(userId, workoutId);

        redisUtil.deleteKey(favoriteKey);

        FavoriteEvent event = new FavoriteEvent(userId, workoutId, "REMOVE", LocalDateTime.now());
        statsEventProducer.sendFavoriteEvent(event);

        redisUtil.incrementHash(
            RedisConstants.WORKOUT_STATS_KEY_PREFIX + workoutId,
            "favoriteCount",
            -1
        );

        log.info("用户{}取消收藏教程{}", userId, workoutId);
    }

    @Override
    public Boolean isFavorited(Long userId, Long workoutId) {
        String favoriteKey = RedisConstants.USER_FAVORITE_KEY_PREFIX + userId + ":" + workoutId;

        Boolean cached = redisUtil.getBooleanValue(favoriteKey);

        if (cached != null) {
            return cached;
        }

        boolean exists = favoriteRepository.existsByUserIdAndWorkoutId(userId, workoutId);

        redisUtil.setBooleanValue(
            favoriteKey,
            exists,
            RedisConstants.FAVORITE_CACHE_EXPIRE_DAYS,
            TimeUnit.DAYS
        );

        return exists;
    }

    @Override
    public List<FavoriteDTO> getUserFavorites(Long userId) {
        List<WorkoutFavorite> favorites = favoriteRepository.findByUserId(userId);
        return favorites.stream()
                .map(favorite -> {
                    FavoriteDTO dto = new FavoriteDTO();
                    BeanUtils.copyProperties(favorite, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Long getFavoriteCount(Long workoutId) {
        String favoriteKey = RedisConstants.USER_FAVORITE_KEY_PREFIX + "*:" + workoutId;

        return favoriteRepository.countByWorkoutId(workoutId);
    }
}

