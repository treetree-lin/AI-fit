package org.lin.fitnessworkout.service;

/**
 * @author lin
 * @date 2026-03-25
 */

import org.lin.fitnessworkout.dto.CommentDTO;
import org.lin.fitnessworkout.dto.FavoriteDTO;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public interface FavoriteService {

    FavoriteDTO addFavorite(Long userId, Long workoutId);

    void removeFavorite(Long userId, Long workoutId);

    Boolean isFavorited(Long userId, Long workoutId);

    List<FavoriteDTO> getUserFavorites(Long userId);

    Long getFavoriteCount(Long workoutId);
}
