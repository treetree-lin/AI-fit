package org.lin.fitnessrecord.service;
import org.lin.fitnessrecord.dto.WorkoutRecordDTO;
import org.lin.fitnesscommon.entity.WorkoutRecord;
import org.lin.fitnesscommon.entity.WorkoutRecordStep;
import org.lin.fitnessrecord.repository.WorkoutRecordRepository;
import org.lin.fitnessrecord.repository.WorkoutRecordStepRepository;
import org.lin.fitnessuser.repository.UserProfileRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lin
 * @date 2026-03-24
 */


@Service
public class WorkoutRecordServiceImpl implements WorkoutRecordService {

    @Autowired
    private WorkoutRecordRepository workoutRecordRepository;

    @Autowired
    private WorkoutRecordStepRepository workoutRecordStepRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public WorkoutRecordDTO createRecord(WorkoutRecordDTO request) {
        WorkoutRecord record = new WorkoutRecord();
        BeanUtils.copyProperties(request, record);

        // 如果 recordDate 为 null，使用当前日期
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }
        
        if (record.getCompleted() == null) {
            record.setCompleted(true);
        }

        // 自动计算步数和卡路里
        if (request.getSteps() != null && !request.getSteps().isEmpty()) {
            record.setStepCount(request.getSteps().size());
            int totalCalories = request.getSteps().stream()
                    .mapToInt(stepDTO -> stepDTO.getCaloriesBurned() != null ? stepDTO.getCaloriesBurned() : 0)
                    .sum();
            if (record.getCaloriesBurned() == null || record.getCaloriesBurned() == 0) {
                record.setCaloriesBurned(totalCalories);
            }
        }

        WorkoutRecord saved = workoutRecordRepository.save(record);

        if (request.getSteps() != null && !request.getSteps().isEmpty()) {
            List<WorkoutRecordStep> steps = request.getSteps().stream().map(stepDTO -> {
                WorkoutRecordStep step = new WorkoutRecordStep();
                BeanUtils.copyProperties(stepDTO, step);
                step.setRecordId(saved.getId());
                return step;
            }).collect(Collectors.toList());
            workoutRecordStepRepository.saveAll(steps);
        }

        // 打卡成功后，更新 UserProfile 的连续打卡天数
        updateStreakDays(saved.getUserId(), saved.getRecordDate());

        return convertToDTO(saved);
    }

    @Override
    public List<WorkoutRecordDTO> getUserRecords(Long userId) {
        return workoutRecordRepository.findByUserIdOrderByRecordDateDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkoutRecordDTO> getRecordsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return workoutRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateDesc(
                userId, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public WorkoutRecordDTO getRecordById(Long id) {
        WorkoutRecord record = workoutRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        return convertToDTO(record);
    }

    @Override
    @Transactional
    public WorkoutRecordDTO updateRecord(Long id, WorkoutRecordDTO request) {
        WorkoutRecord record = workoutRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

        BeanUtils.copyProperties(request, record, "id", "userId", "createdAt");

        if (request.getSteps() != null) {
            workoutRecordStepRepository.deleteByRecordId(id);
            List<WorkoutRecordStep> steps = request.getSteps().stream().map(stepDTO -> {
                WorkoutRecordStep step = new WorkoutRecordStep();
                BeanUtils.copyProperties(stepDTO, step);
                step.setRecordId(id);
                return step;
            }).collect(Collectors.toList());
            workoutRecordStepRepository.saveAll(steps);
        }

        WorkoutRecord updated = workoutRecordRepository.save(record);
        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteRecord(Long id) {
        if (!workoutRecordRepository.existsById(id)) {
            throw new RuntimeException("记录不存在");
        }
        workoutRecordStepRepository.deleteByRecordId(id);
        workoutRecordRepository.deleteById(id);
    }

    @Override
    public Map<String, Object> getMonthlyStats(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<WorkoutRecord> records = workoutRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateDesc(userId, start, end);

        Map<String, Object> stats = new HashMap<>();
        stats.put("year", year);
        stats.put("month", month);
        stats.put("totalDays", records.size());
        stats.put("completedDays", records.stream().filter(WorkoutRecord::getCompleted).count());

        int totalMinutes = records.stream()
                .mapToInt(r -> r.getDurationMinutes() != null ? r.getDurationMinutes() : 0)
                .sum();
        stats.put("totalMinutes", totalMinutes);

        int totalCalories = records.stream()
                .mapToInt(r -> r.getCaloriesBurned() != null ? r.getCaloriesBurned() : 0)
                .sum();
        stats.put("totalCalories", totalCalories);

        double avgRating = records.stream()
                .filter(r -> r.getRating() != null)
                .mapToInt(WorkoutRecord::getRating)
                .average()
                .orElse(0.0);
        stats.put("averageRating", avgRating);

        return stats;
    }

    @Override
    public Map<String, Object> getOverviewStats(Long userId) {
        List<WorkoutRecord> allRecords = workoutRecordRepository.findByUserId(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", allRecords.size());
        stats.put("totalWorkouts", allRecords.size());
        stats.put("completedRecords", allRecords.stream().filter(WorkoutRecord::getCompleted).count());

        int totalMinutes = allRecords.stream()
                .mapToInt(r -> r.getDurationMinutes() != null ? r.getDurationMinutes() : 0)
                .sum();
        stats.put("totalMinutes", totalMinutes);

        int totalCalories = allRecords.stream()
                .mapToInt(r -> r.getCaloriesBurned() != null ? r.getCaloriesBurned() : 0)
                .sum();
        stats.put("totalCalories", totalCalories);

        // 计算连续打卡天数
        int streakDays = calculateStreakDays(allRecords);
        stats.put("streakDays", streakDays);

        if (!allRecords.isEmpty()) {
            stats.put("firstRecordDate", allRecords.stream()
                    .min(Comparator.comparing(WorkoutRecord::getRecordDate))
                    .map(WorkoutRecord::getRecordDate)
                    .orElse(null));

            stats.put("lastRecordDate", allRecords.stream()
                    .max(Comparator.comparing(WorkoutRecord::getRecordDate))
                    .map(WorkoutRecord::getRecordDate)
                    .orElse(null));
        }

        return stats;
    }

    private int calculateStreakDays(List<WorkoutRecord> allRecords) {
        if (allRecords.isEmpty()) return 0;
        List<LocalDate> dates = allRecords.stream()
                .map(WorkoutRecord::getRecordDate)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        if (!dates.get(0).equals(today) && !dates.get(0).equals(yesterday)) return 0;
        int streak = 1;
        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i).equals(dates.get(i - 1).minusDays(1))) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    /**
     * 更新用户的连续打卡天数和最后打卡日期
     */
    private void updateStreakDays(Long userId, LocalDate recordDate) {
        try {
            // 获取用户所有记录
            List<WorkoutRecord> allRecords = workoutRecordRepository.findByUserId(userId);
            
            // 计算连续打卡天数
            int streakDays = calculateStreakDays(allRecords);
            
            // 更新 UserProfile
            userProfileRepository.updateStreakDays(userId, streakDays, recordDate);
            
            System.out.println("更新打卡信息: userId=" + userId + ", streakDays=" + streakDays + ", lastCheckInDate=" + recordDate);
        } catch (Exception e) {
            System.err.println("更新打卡信息失败: " + e.getMessage());
            // 不抛出异常，避免影响主流程
        }
    }

    @Override
    public List<Map<String, Object>> getLast7DaysStats(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);

        List<WorkoutRecord> records = workoutRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateDesc(
                userId, sevenDaysAgo, today);

        Map<LocalDate, List<WorkoutRecord>> grouped = records.stream()
                .collect(Collectors.groupingBy(WorkoutRecord::getRecordDate));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<WorkoutRecord> dayRecords = grouped.getOrDefault(date, Collections.emptyList());

            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", date);
            dayStat.put("recordCount", dayRecords.size());
            dayStat.put("completedCount", dayRecords.stream().filter(WorkoutRecord::getCompleted).count());

            int totalMinutes = dayRecords.stream()
                    .mapToInt(r -> r.getDurationMinutes() != null ? r.getDurationMinutes() : 0)
                    .sum();
            dayStat.put("durationMinutes", totalMinutes);

            int totalCalories = dayRecords.stream()
                    .mapToInt(r -> r.getCaloriesBurned() != null ? r.getCaloriesBurned() : 0)
                    .sum();
            dayStat.put("caloriesBurned", totalCalories);

            result.add(dayStat);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getHeatmapStats(Long userId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);
        List<WorkoutRecord> records = workoutRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateDesc(
                userId, startDate, today);
        Map<LocalDate, Long> grouped = records.stream()
                .collect(Collectors.groupingBy(WorkoutRecord::getRecordDate, Collectors.counting()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long count = grouped.getOrDefault(date, 0L);
            Map<String, Object> day = new HashMap<>();
            day.put("date", date.toString());
            day.put("count", count);
            day.put("level", Math.min(4, (int) count));
            result.add(day);
        }
        return result;
    }

    @Override
    public Map<String, Object> getRadarStats(Long userId) {
        List<WorkoutRecord> allRecords = workoutRecordRepository.findByUserId(userId);
        Map<String, Object> radar = new HashMap<>();
        // 模拟雷达图维度数据，基于训练记录计算
        int strengthCount = (int) allRecords.stream()
                .filter(r -> r.getWorkoutId() != null && r.getWorkoutId() % 3 == 0).count();
        int enduranceCount = (int) allRecords.stream()
                .filter(r -> r.getWorkoutId() != null && r.getWorkoutId() % 3 == 1).count();
        int flexibilityCount = (int) allRecords.stream()
                .filter(r -> r.getWorkoutId() != null && r.getWorkoutId() % 3 == 2).count();
        int total = Math.max(1, allRecords.size());
        radar.put("strength", Math.min(100, strengthCount * 100 / total + 30));
        radar.put("endurance", Math.min(100, enduranceCount * 100 / total + 30));
        radar.put("flexibility", Math.min(100, flexibilityCount * 100 / total + 30));
        radar.put("speed", Math.min(100, totalMinutes(allRecords) / 10 + 20));
        radar.put("coordination", Math.min(100, total / 2 + 20));
        radar.put("balance", Math.min(100, total / 2 + 15));
        return radar;
    }

    private int totalMinutes(List<WorkoutRecord> records) {
        return records.stream().mapToInt(r -> r.getDurationMinutes() != null ? r.getDurationMinutes() : 0).sum();
    }

    private WorkoutRecordDTO convertToDTO(WorkoutRecord record) {
        WorkoutRecordDTO dto = new WorkoutRecordDTO();
        dto.setId(record.getId());
        dto.setUserId(record.getUserId());
        dto.setWorkoutId(record.getWorkoutId());
        dto.setRecordDate(record.getRecordDate());
        dto.setDurationMinutes(record.getDurationMinutes());
        dto.setCaloriesBurned(record.getCaloriesBurned());
        dto.setCompleted(record.getCompleted());
        dto.setRating(record.getRating());
        dto.setNotes(record.getNotes());
        dto.setStepCount(record.getStepCount());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUpdatedAt(record.getUpdatedAt());

        List<WorkoutRecordStep> steps = workoutRecordStepRepository.findByRecordId(record.getId());
        if (steps != null && !steps.isEmpty()) {
            dto.setSteps(steps.stream().map(s -> {
                WorkoutRecordDTO.StepDTO stepDTO = new WorkoutRecordDTO.StepDTO();
                stepDTO.setId(s.getId());
                stepDTO.setStepId(s.getStepId());
                stepDTO.setExerciseName(s.getExerciseName());
                stepDTO.setActualSets(s.getActualSets());
                stepDTO.setActualReps(s.getActualReps());
                stepDTO.setWeightUsed(s.getWeightUsed());
                return stepDTO;
            }).collect(Collectors.toList()));
        }
        return dto;
    }
}
