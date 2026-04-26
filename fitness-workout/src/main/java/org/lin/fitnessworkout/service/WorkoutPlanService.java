package org.lin.fitnessworkout.service;

import org.lin.fitnessworkout.dto.WorkoutPlanDTO;

import java.util.List;

public interface WorkoutPlanService {

    WorkoutPlanDTO createPlan(WorkoutPlanDTO dto, Long userId);

    WorkoutPlanDTO getPlanById(Long planId);

    WorkoutPlanDTO getActivePlanByUser(Long userId);

    List<WorkoutPlanDTO> getPlansByUser(Long userId);

    WorkoutPlanDTO updatePlan(Long planId, WorkoutPlanDTO dto);

    void deletePlan(Long planId);

    WorkoutPlanDTO updateItemStatus(Long itemId, String status);
}
