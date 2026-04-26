package org.lin.fitnessworkout.repository;

/**
 * @author lin
 * @date 2026-03-25
 */

import org.lin.fitnesscommon.entity.WorkoutComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface WorkoutCommentRepository extends JpaRepository<WorkoutComment, Long> {

    List<WorkoutComment> findByWorkoutIdOrderByCreatedAtDesc(Long workoutId);

    List<WorkoutComment> findByWorkoutIdAndParentIdIsNullOrderByCreatedAtDesc(Long workoutId);

    List<WorkoutComment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    Long countByWorkoutId(Long workoutId);

    Long countByUserId(Long userId);
    @Modifying
    @Transactional
    @Query("UPDATE WorkoutComment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE WorkoutComment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :id AND c.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE WorkoutComment c SET c.replyCount = c.replyCount + 1 WHERE c.id = :id")
    void incrementReplyCount(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE WorkoutComment c SET c.replyCount = c.replyCount - 1 WHERE c.id = :id AND c.replyCount > 0")
    void decrementReplyCount(@Param("id") Long id);
}

