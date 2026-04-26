package org.lin.fitnessworkout.controller;

/**
 * @author lin
 * @date 2026-03-25
 */

import lombok.extern.slf4j.Slf4j;
import org.lin.fitnesscommon.vo.ApiResponse;
import org.lin.fitnessworkout.dto.CommentDTO;
import org.lin.fitnessworkout.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/workouts/comments")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ApiResponse<CommentDTO> addComment(
            @RequestParam Long userId,
            @RequestParam String username,
            @RequestParam Long workoutId,
            @RequestParam(required = false) Long parentId,
            @RequestBody Map<String, String> body) {
        try {
            String content = body.get("content");
            CommentDTO comment = commentService.addComment(userId, username, workoutId, parentId, content);
            return ApiResponse.success("评论成功", comment);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<String> deleteComment(@PathVariable Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return ApiResponse.success("删除评论成功", "success");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{workoutId}")
    public ApiResponse<List<CommentDTO>> getComments(@PathVariable Long workoutId) {
        List<CommentDTO> comments = commentService.getCommentsByWorkout(workoutId);
        return ApiResponse.success(comments);
    }

    @PostMapping("/{commentId}/like")
    public ApiResponse<CommentDTO> toggleLike(
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        try {
            CommentDTO comment = commentService.toggleLike(userId, commentId);
            return ApiResponse.success("操作成功", comment);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
