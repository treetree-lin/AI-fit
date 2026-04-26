package org.lin.fitnessworkout.service;

import org.lin.fitnesscommon.entity.WorkoutPlan;
import org.lin.fitnesscommon.entity.WorkoutPlanItem;
import org.lin.fitnessworkout.dto.WorkoutPlanDTO;
import org.lin.fitnessworkout.repository.WorkoutPlanItemRepository;
import org.lin.fitnessworkout.repository.WorkoutPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkoutPlanServiceImpl implements WorkoutPlanService {

    @Autowired
    private WorkoutPlanRepository planRepository;

    @Autowired
    private WorkoutPlanItemRepository itemRepository;

    @Override
    @Transactional
    public WorkoutPlanDTO createPlan(WorkoutPlanDTO dto, Long userId) {
        WorkoutPlan plan = new WorkoutPlan();
        plan.setUserId(userId);
        plan.setName(dto.getName());
        plan.setDescription(dto.getDescription());
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        plan.setStatus(WorkoutPlan.PlanStatus.ACTIVE);

        plan = planRepository.save(plan);

        if (dto.getItems() != null) {
            List<WorkoutPlanItem> items = new ArrayList<>();
            for (WorkoutPlanDTO.PlanItemDTO itemDto : dto.getItems()) {
                WorkoutPlanItem item = new WorkoutPlanItem();
                item.setPlanId(plan.getId());
                item.setDayOfWeek(itemDto.getDayOfWeek());
                if (itemDto.getExerciseType() != null) {
                    item.setExerciseType(WorkoutPlanItem.ExerciseType.valueOf(itemDto.getExerciseType()));
                }
                item.setDurationMinutes(itemDto.getDurationMinutes());
                item.setCaloriesBurned(itemDto.getCaloriesBurned());
                item.setScheduledDate(itemDto.getScheduledDate());
                item.setStatus(WorkoutPlanItem.ItemStatus.PENDING);
                items.add(item);
            }
            itemRepository.saveAll(items);
        }

        return convertToDTO(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutPlanDTO getPlanById(Long planId) {
        WorkoutPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("计划不存在"));
        return convertToDTO(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutPlanDTO getActivePlanByUser(Long userId) {
        WorkoutPlan plan = planRepository.findByUserIdAndStatus(userId, WorkoutPlan.PlanStatus.ACTIVE)
                .orElse(null);
        return plan != null ? convertToDTO(plan) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutPlanDTO> getPlansByUser(Long userId) {
        return planRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkoutPlanDTO updatePlan(Long planId, WorkoutPlanDTO dto) {
        WorkoutPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("计划不存在"));

        if (dto.getName() != null) plan.setName(dto.getName());
        if (dto.getDescription() != null) plan.setDescription(dto.getDescription());
        if (dto.getStartDate() != null) plan.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) plan.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) plan.setStatus(WorkoutPlan.PlanStatus.valueOf(dto.getStatus()));

        if (dto.getItems() != null) {
            itemRepository.deleteByPlanId(planId);
            List<WorkoutPlanItem> items = new ArrayList<>();
            for (WorkoutPlanDTO.PlanItemDTO itemDto : dto.getItems()) {
                WorkoutPlanItem item = new WorkoutPlanItem();
                item.setPlanId(planId);
                item.setDayOfWeek(itemDto.getDayOfWeek());
                if (itemDto.getExerciseType() != null) {
                    item.setExerciseType(WorkoutPlanItem.ExerciseType.valueOf(itemDto.getExerciseType()));
                }
                item.setDurationMinutes(itemDto.getDurationMinutes());
                item.setCaloriesBurned(itemDto.getCaloriesBurned());
                item.setScheduledDate(itemDto.getScheduledDate());
                item.setStatus(itemDto.getStatus() != null
                        ? WorkoutPlanItem.ItemStatus.valueOf(itemDto.getStatus())
                        : WorkoutPlanItem.ItemStatus.PENDING);
                items.add(item);
            }
            itemRepository.saveAll(items);
        }

        plan = planRepository.save(plan);
        return convertToDTO(plan);
    }

    @Override
    @Transactional
    public void deletePlan(Long planId) {
        itemRepository.deleteByPlanId(planId);
        planRepository.deleteById(planId);
    }

    @Override
    @Transactional
    public WorkoutPlanDTO updateItemStatus(Long itemId, String status) {
        WorkoutPlanItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("计划项不存在"));
        item.setStatus(WorkoutPlanItem.ItemStatus.valueOf(status));
        itemRepository.save(item);
        return getPlanById(item.getPlanId());
    }

    private WorkoutPlanDTO convertToDTO(WorkoutPlan plan) {
        WorkoutPlanDTO dto = new WorkoutPlanDTO();
        dto.setId(plan.getId());
        dto.setUserId(plan.getUserId());
        dto.setName(plan.getName());
        dto.setDescription(plan.getDescription());
        dto.setStartDate(plan.getStartDate());
        dto.setEndDate(plan.getEndDate());
        dto.setStatus(plan.getStatus().name());
        dto.setCreatedAt(plan.getCreatedAt());
        dto.setUpdatedAt(plan.getUpdatedAt());

        List<WorkoutPlanItem> items = itemRepository.findByPlanIdOrderByDayOfWeekAsc(plan.getId());
        List<WorkoutPlanDTO.PlanItemDTO> itemDTOs = new ArrayList<>();
        for (WorkoutPlanItem item : items) {
            WorkoutPlanDTO.PlanItemDTO itemDto = new WorkoutPlanDTO.PlanItemDTO();
            itemDto.setId(item.getId());
            itemDto.setDayOfWeek(item.getDayOfWeek());
            if (item.getExerciseType() != null) {
                itemDto.setExerciseType(item.getExerciseType().name());
                itemDto.setExerciseTypeLabel(item.getExerciseType().getLabel());
            }
            itemDto.setDurationMinutes(item.getDurationMinutes());
            itemDto.setCaloriesBurned(item.getCaloriesBurned());
            itemDto.setStatus(item.getStatus().name());
            itemDto.setScheduledDate(item.getScheduledDate());
            itemDTOs.add(itemDto);
        }
        dto.setItems(itemDTOs);
        return dto;
    }
}
