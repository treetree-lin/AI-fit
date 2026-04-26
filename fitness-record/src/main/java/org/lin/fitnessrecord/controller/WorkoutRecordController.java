package org.lin.fitnessrecord.controller;
import org.lin.fitnesscommon.vo.ApiResponse;
import org.lin.fitnessrecord.dto.WorkoutRecordDTO;
import org.lin.fitnessrecord.service.WorkoutRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author lin
 * @date 2026-03-24
 */


@RestController
@RequestMapping("/api/records")
@CrossOrigin(origins = "*")
public class WorkoutRecordController {

    @Autowired
    private WorkoutRecordService workoutRecordService;

    /**
     * 打卡 - 创建运动记录
     */
    @PostMapping("/checkin")
    public ResponseEntity<ApiResponse<WorkoutRecordDTO>> checkIn(@RequestBody WorkoutRecordDTO request) {
        System.out.println("用户打卡请求：" + request);
        System.out.println("打卡日期 recordDate: " + request.getRecordDate());
        try {
            WorkoutRecordDTO record = workoutRecordService.createRecord(request);
            return ResponseEntity.ok(ApiResponse.success("打卡成功", record));
        } catch (Exception e) {
            System.err.println("打卡失败：" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error("打卡失败：" + e.getMessage()));
        }
    }

    /**
     * 获取用户的所有记录
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<WorkoutRecordDTO>> getUserRecords(@PathVariable Long userId) {
        System.out.println("获取用户" + userId + "的记录");
        List<WorkoutRecordDTO> records = workoutRecordService.getUserRecords(userId);
        return ApiResponse.success(records);
    }

    /**
     * 根据日期范围查询记录
     */
    @GetMapping("/user/{userId}/date-range")
    public ApiResponse<List<WorkoutRecordDTO>> getRecordsByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        System.out.println("查询用户" + userId + "从" + startDate + "到" + endDate + "的记录");
        List<WorkoutRecordDTO> records = workoutRecordService.getRecordsByDateRange(userId, startDate, endDate);
        return ApiResponse.success(records);
    }

    /**
     * 获取单条记录详情
     */
    @GetMapping("/{id}")
    public ApiResponse<WorkoutRecordDTO> getRecordById(@PathVariable Long id) {
        System.out.println("获取记录详情：" + id);
        try {
            WorkoutRecordDTO record = workoutRecordService.getRecordById(id);
            return ApiResponse.success(record);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkoutRecordDTO>> updateRecord(@PathVariable Long id, @RequestBody WorkoutRecordDTO request) {
        System.out.println("更新记录：" + id);
        try {
            WorkoutRecordDTO updated = workoutRecordService.updateRecord(id, request);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updated));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("更新失败：" + e.getMessage()));
        }
    }

    /**
     * 删除记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRecord(@PathVariable Long id) {
        System.out.println("删除记录：" + id);
        try {
            workoutRecordService.deleteRecord(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("删除失败：" + e.getMessage()));
        }
    }

    /**
     * 统计接口 - 用户月度统计
     */
    @GetMapping("/stats/monthly/{userId}")
    public ApiResponse<Map<String, Object>> getMonthlyStats(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        System.out.println("获取用户" + userId + "的" + year + "年" + month + "月统计");
        Map<String, Object> stats = workoutRecordService.getMonthlyStats(userId, year, month);
        return ApiResponse.success(stats);
    }

    /**
     * 统计接口 - 用户总览统计
     */
    @GetMapping("/stats/overview/{userId}")
    public ApiResponse<Map<String, Object>> getOverviewStats(@PathVariable Long userId) {
        System.out.println("获取用户" + userId + "的总览统计");
        Map<String, Object> stats = workoutRecordService.getOverviewStats(userId);
        return ApiResponse.success(stats);
    }

    /**
     * 统计接口 - 最近 7 天记录数
     */
    @GetMapping("/stats/last-7days/{userId}")
    public ApiResponse<List<Map<String, Object>>> getLast7DaysStats(@PathVariable Long userId) {
        System.out.println("获取用户" + userId + "最近 7 天统计");
        List<Map<String, Object>> stats = workoutRecordService.getLast7DaysStats(userId);
        return ApiResponse.success(stats);
    }

    /**
     * 统计接口 - 热力图数据
     */
    @GetMapping("/stats/heatmap/{userId}")
    public ApiResponse<List<Map<String, Object>>> getHeatmapStats(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        System.out.println("获取用户" + userId + "热力图数据，天数：" + days);
        List<Map<String, Object>> stats = workoutRecordService.getHeatmapStats(userId, days);
        return ApiResponse.success(stats);
    }

    /**
     * 统计接口 - 雷达图数据
     */
    @GetMapping("/stats/radar/{userId}")
    public ApiResponse<Map<String, Object>> getRadarStats(@PathVariable Long userId) {
        System.out.println("获取用户" + userId + "雷达图数据");
        Map<String, Object> stats = workoutRecordService.getRadarStats(userId);
        return ApiResponse.success(stats);
    }
}
