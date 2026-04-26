package org.lin.fitnessworkout.controller;

/**
 * @author lin
 * @date 2026-03-25
 */

import lombok.extern.slf4j.Slf4j;
import org.lin.fitnesscommon.vo.ApiResponse;
import org.lin.fitnessworkout.dto.FavoriteDTO;
import org.lin.fitnessworkout.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/workouts/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/{workoutId}")
    public ApiResponse<Map<String, Object>> addFavorite(
            @PathVariable Long workoutId,
            @RequestParam Long userId) {
        try {
            FavoriteDTO favorite = favoriteService.addFavorite(userId, workoutId);

            Map<String, Object> response = new HashMap<>();
            response.put("favorite", favorite);
            response.put("message", "收藏成功");

            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{workoutId}")
    public ApiResponse<String> removeFavorite(
            @PathVariable Long workoutId,
            @RequestParam Long userId) {
        try {
            favoriteService.removeFavorite(userId, workoutId);
            return ApiResponse.success("取消收藏成功", "success");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{workoutId}/check")
    public ApiResponse<Map<String, Boolean>> checkFavorite(
            @PathVariable Long workoutId,
            @RequestParam Long userId) {
        Boolean isFavorited = favoriteService.isFavorited(userId, workoutId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isFavorited", isFavorited);

        return ApiResponse.success(response);
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<FavoriteDTO>> getUserFavorites(@PathVariable Long userId) {
        List<FavoriteDTO> favorites = favoriteService.getUserFavorites(userId);
        return ApiResponse.success(favorites);
    }
}
