package org.lin.fitnessworkout.service;

/**
 * @author lin
 * @date 2026-03-25
 */

import org.lin.fitnessworkout.dto.CommentDTO;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public interface CommentService {

    CommentDTO addComment(Long userId, String username, Long workoutId, Long parentId, String content);

    void deleteComment(Long commentId);

    List<CommentDTO> getCommentsByWorkout(Long workoutId);

    CommentDTO toggleLike(Long userId, Long commentId);

    Long getCommentCount(Long workoutId);
}
