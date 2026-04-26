package org.lin.fitnessworkout.repository;

/**
 * @author lin
 * @date 2026-03-25
 */

import org.lin.fitnesscommon.entity.WorkoutFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutFavoriteRepository extends JpaRepository<WorkoutFavorite, Long> {

    Optional<WorkoutFavorite> findByUserIdAndWorkoutId(Long userId, Long workoutId);

    List<WorkoutFavorite> findByUserId(Long userId);

    List<WorkoutFavorite> findByWorkoutId(Long workoutId);

    Long countByWorkoutId(Long workoutId);

    boolean existsByUserIdAndWorkoutId(Long userId, Long workoutId);

    void deleteByUserIdAndWorkoutId(Long userId, Long workoutId);
}
