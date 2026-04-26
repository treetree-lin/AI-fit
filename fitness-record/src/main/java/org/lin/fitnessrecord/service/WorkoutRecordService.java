package org.lin.fitnessrecord.service;

import org.lin.fitnessrecord.dto.WorkoutRecordDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
/**
 * @author lin
 * @date 2026-03-24
 */



public interface WorkoutRecordService {

    WorkoutRecordDTO createRecord(WorkoutRecordDTO request);

    List<WorkoutRecordDTO> getUserRecords(Long userId);

    List<WorkoutRecordDTO> getRecordsByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    WorkoutRecordDTO getRecordById(Long id);

    WorkoutRecordDTO updateRecord(Long id, WorkoutRecordDTO request);

    void deleteRecord(Long id);

    Map<String, Object> getMonthlyStats(Long userId, int year, int month);

    Map<String, Object> getOverviewStats(Long userId);

    List<Map<String, Object>> getLast7DaysStats(Long userId);

    List<Map<String, Object>> getHeatmapStats(Long userId, int days);

    Map<String, Object> getRadarStats(Long userId);
}

