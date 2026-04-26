package org.lin.fitnessworkout.controller;

import org.lin.fitnesscommon.vo.ApiResponse;
import org.lin.fitnessworkout.dto.WorkoutPlanDTO;
import org.lin.fitnessworkout.service.WorkoutPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@CrossOrigin(origins = "*")
public class WorkoutPlanController {

    @Autowired
    private WorkoutPlanService planService;

    @PostMapping
    public ApiResponse<WorkoutPlanDTO> createPlan(@RequestBody WorkoutPlanDTO dto, @RequestParam Long userId) {
        try {
            WorkoutPlanDTO created = planService.createPlan(dto, userId);
            return ApiResponse.success("创建计划成功", created);
        } catch (Exception e) {
            return ApiResponse.error("创建计划失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkoutPlanDTO> getPlanById(@PathVariable Long id) {
        try {
            WorkoutPlanDTO plan = planService.getPlanById(id);
            return ApiResponse.success(plan);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<WorkoutPlanDTO>> getPlansByUser(@PathVariable Long userId) {
        List<WorkoutPlanDTO> plans = planService.getPlansByUser(userId);
        return ApiResponse.success(plans);
    }

    @GetMapping("/user/{userId}/active")
    public ApiResponse<WorkoutPlanDTO> getActivePlan(@PathVariable Long userId) {
        WorkoutPlanDTO plan = planService.getActivePlanByUser(userId);
        if (plan == null) {
            return ApiResponse.success("暂无活跃计划", null);
        }
        return ApiResponse.success(plan);
    }

    @PutMapping("/{id}")
    public ApiResponse<WorkoutPlanDTO> updatePlan(@PathVariable Long id, @RequestBody WorkoutPlanDTO dto) {
        try {
            WorkoutPlanDTO updated = planService.updatePlan(id, dto);
            return ApiResponse.success("更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deletePlan(@PathVariable Long id) {
        try {
            planService.deletePlan(id);
            return ApiResponse.success("删除成功", "success");
        } catch (Exception e) {
            return ApiResponse.error("删除失败：" + e.getMessage());
        }
    }

    @PatchMapping("/items/{itemId}/status")
    public ApiResponse<WorkoutPlanDTO> updateItemStatus(
            @PathVariable Long itemId,
            @RequestParam String status) {
        try {
            WorkoutPlanDTO updated = planService.updateItemStatus(itemId, status);
            return ApiResponse.success("状态更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error("更新失败：" + e.getMessage());
        }
    }
}
