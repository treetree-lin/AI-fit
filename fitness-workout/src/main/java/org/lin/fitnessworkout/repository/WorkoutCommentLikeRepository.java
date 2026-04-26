package org.lin.fitnessworkout.repository;

/**
 * @author lin
 * @date 2026-03-25
 */

import org.lin.fitnesscommon.entity.WorkoutCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkoutCommentLikeRepository extends JpaRepository<WorkoutCommentLike, Long> {

    Optional<WorkoutCommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    void deleteByUserIdAndCommentId(Long userId, Long commentId);

    Long countByCommentId(Long commentId);
}
