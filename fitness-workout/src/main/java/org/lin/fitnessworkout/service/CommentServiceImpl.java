package org.lin.fitnessworkout.service;

/**
 * @author lin
 * @date 2026-03-25
 */

import lombok.extern.slf4j.Slf4j;
import org.lin.fitnesscommon.constant.RedisConstants;
import org.lin.fitnessworkout.dto.CommentDTO;
import org.lin.fitnesscommon.entity.Workout;
import org.lin.fitnesscommon.entity.WorkoutComment;
import org.lin.fitnesscommon.entity.WorkoutCommentLike;
import org.lin.fitnessworkout.repository.WorkoutCommentLikeRepository;
import org.lin.fitnessworkout.repository.WorkoutCommentRepository;
import org.lin.fitnessworkout.repository.WorkoutRepository;
import org.lin.fitnesscommon.utils.RedisUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private WorkoutCommentRepository commentRepository;

    @Autowired
    private WorkoutCommentLikeRepository commentLikeRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Transactional
    public CommentDTO addComment(Long userId, String username, Long workoutId, Long parentId, String content) {
        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("健身方案不存在"));

        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("评论内容不能为空");
        }

        if (content.length() > 1000) {
            throw new RuntimeException("评论内容不能超过 1000 字");
        }

        WorkoutComment comment = new WorkoutComment();
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setWorkoutId(workoutId);
        comment.setParentId(parentId);
        comment.setContent(content);
        comment.setLikeCount(0L);
        comment.setReplyCount(0L);

        WorkoutComment savedComment = commentRepository.save(comment);

        if (parentId != null) {
            commentRepository.incrementReplyCount(parentId);
            log.info("用户{}回复评论{}", userId, parentId);
        } else {
            log.info("用户{}评论教程{}，内容：{}", userId, workoutId, content);
        }

        return convertToDTO(savedComment, userId);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        WorkoutComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        Long workoutId = comment.getWorkoutId();
        Long userId = comment.getUserId();

        if (comment.getParentId() != null) {
            commentRepository.decrementReplyCount(comment.getParentId());

            List<WorkoutComment> childComments = commentRepository.findByParentIdOrderByCreatedAtAsc(commentId);
            for (WorkoutComment child : childComments) {
                commentRepository.delete(child);
            }
        } else {
            List<WorkoutComment> allReplies = commentRepository.findByParentIdOrderByCreatedAtAsc(commentId);
            for (WorkoutComment reply : allReplies) {
                commentRepository.delete(reply);
            }
        }

        commentRepository.delete(comment);

        log.info("删除评论：{}", commentId);
    }

    @Override
    public List<CommentDTO> getCommentsByWorkout(Long workoutId) {
        List<WorkoutComment> parentComments = commentRepository.findByWorkoutIdAndParentIdIsNullOrderByCreatedAtDesc(workoutId);

        return parentComments.stream()
                .map(comment -> convertToDTO(comment, null))
                .peek(dto -> {
                    List<WorkoutComment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(dto.getId());
                    if (!replies.isEmpty()) {
                        List<CommentDTO> replyDTOs = replies.stream()
                                .map(reply -> convertToDTO(reply, null))
                                .collect(Collectors.toList());
                        dto.setReplies(replyDTOs);
                        dto.setReplyCount((long) replyDTOs.size());
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDTO toggleLike(Long userId, Long commentId) {
        String likeKey = RedisConstants.COMMENT_LIKE_KEY_PREFIX + userId + ":" + commentId;

        Boolean isLiked = redisUtil.getBooleanValue(likeKey);

        if (Boolean.TRUE.equals(isLiked)) {
            commentLikeRepository.deleteByUserIdAndCommentId(userId, commentId);

            redisUtil.deleteKey(likeKey);

            commentRepository.decrementLikeCount(commentId);

            log.info("用户{}取消点赞评论{}", userId, commentId);
        } else {
            boolean existsInDb = commentLikeRepository.existsByUserIdAndCommentId(userId, commentId);

            if (!existsInDb) {
                WorkoutCommentLike like = new WorkoutCommentLike();
                like.setUserId(userId);
                like.setCommentId(commentId);
                commentLikeRepository.save(like);
            }

            redisUtil.setBooleanValue(
                likeKey,
                true,
                RedisConstants.COMMENT_LIKE_CACHE_EXPIRE_DAYS,
                TimeUnit.DAYS
            );

            commentRepository.incrementLikeCount(commentId);

            log.info("用户{}点赞评论{}", userId, commentId);
        }

        WorkoutComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        return convertToDTO(comment, userId);
    }

    @Override
    public Long getCommentCount(Long workoutId) {
        return commentRepository.countByWorkoutId(workoutId);
    }

    private CommentDTO convertToDTO(WorkoutComment comment, Long currentUserId) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);

        if (currentUserId != null) {
            String likeKey = RedisConstants.COMMENT_LIKE_KEY_PREFIX + currentUserId + ":" + comment.getId();
            Boolean isLiked = redisUtil.getBooleanValue(likeKey);

            if (isLiked == null) {
                isLiked = commentLikeRepository.existsByUserIdAndCommentId(currentUserId, comment.getId());
                redisUtil.setBooleanValue(
                    likeKey,
                    isLiked,
                    RedisConstants.COMMENT_LIKE_CACHE_EXPIRE_DAYS,
                    TimeUnit.DAYS
                );
            }

            dto.setIsLiked(isLiked);
        } else {
            dto.setIsLiked(false);
        }

        return dto;
    }
}
