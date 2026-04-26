package org.lin.fitnessworkout.dto;

/**
 * @author lin
 * @date 2026-03-25
 */

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDTO {

    private Long id;
    private Long userId;
    private String username;
    private Long workoutId;
    private Long parentId;
    private String content;
    private Long likeCount;
    private Long replyCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isLiked;
    private List<CommentDTO> replies;
}
