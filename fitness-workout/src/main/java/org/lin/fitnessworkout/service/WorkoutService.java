package org.lin.fitnessworkout.service;

import org.lin.fitnessworkout.dto.WorkoutDTO;
import java.util.List;
/**
 * @author lin
 * @date 2026-03-24
 */


public interface WorkoutService {

    WorkoutDTO createWorkout(WorkoutDTO request, Long adminId);

    List<WorkoutDTO> getAllActiveWorkouts();

    WorkoutDTO getWorkoutById(Long id);

    List<WorkoutDTO> getByDifficulty(String difficulty);

    List<WorkoutDTO> getByTargetMuscle(String targetMuscle);

    WorkoutDTO updateWorkout(Long id, WorkoutDTO request);

    void deleteWorkout(Long id);

    WorkoutDTO toggleWorkoutStatus(Long id);
    void incrementFavoriteCount(Long workoutId);

    void decrementFavoriteCount(Long workoutId);

    void incrementCommentCount(Long workoutId);

    void decrementCommentCount(Long workoutId);

    void incrementViewCount(Long workoutId);
}


