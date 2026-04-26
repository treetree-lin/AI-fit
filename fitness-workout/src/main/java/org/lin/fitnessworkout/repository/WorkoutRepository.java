package org.lin.fitnessworkout.repository;
import jakarta.transaction.Transactional;
import org.lin.fitnesscommon.entity.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author lin
 * @date 2026-03-24
 */


@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    List<Workout> findByIsActiveTrue();

    List<Workout> findByDifficultyAndIsActiveTrue(Workout.Difficulty difficulty);

    List<Workout> findByTargetMuscleContainingIgnoreCaseAndIsActiveTrue(String targetMuscle);

    @Modifying
    @Transactional
    @Query("UPDATE Workout w SET w.favoriteCount = w.favoriteCount + 1 WHERE w.id = :id")
    void incrementFavoriteCount(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Workout w SET w.favoriteCount = w.favoriteCount - 1 WHERE w.id = :id AND w.favoriteCount > 0")
    void decrementFavoriteCount(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Workout w SET w.commentCount = w.commentCount + 1 WHERE w.id = :id")
    void incrementCommentCount(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Workout w SET w.commentCount = w.commentCount - 1 WHERE w.id = :id AND w.commentCount > 0")
    void decrementCommentCount(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Workout w SET w.viewCount = :count WHERE w.id = :id")
    void updateViewCount(@Param("id") Long id, @Param("count") Long count);
}