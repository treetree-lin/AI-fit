package org.lin.fitnessworkout.controller;
import org.lin.fitnesscommon.utils.MinioUtil;
import org.lin.fitnesscommon.vo.ApiResponse;
import org.lin.fitnessworkout.dto.WorkoutDTO;
import org.lin.fitnessworkout.service.WorkoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lin
 * @date 2026-03-24
 */



@RestController
@RequestMapping("/api/workouts")
@CrossOrigin(origins = "*")
public class WorkoutController {
Logger log = LoggerFactory.getLogger(WorkoutController.class);
    @Autowired
    private WorkoutService workoutService;
    @Autowired
    private MinioUtil minioUtil;

    /**
     * 创建健身方案（管理员）
     */
    @PostMapping
    public ApiResponse<WorkoutDTO> createWorkout(@RequestBody WorkoutDTO request, @RequestParam Long adminId) {
        System.out.println("管理员创建健身方案：" + request);
        try {
            WorkoutDTO created = workoutService.createWorkout(request, adminId);
            return ApiResponse.success("创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有健身方案
     */
    @GetMapping
    public ApiResponse<List<WorkoutDTO>> getAllWorkouts() {
        System.out.println("获取所有健身方案");
        List<WorkoutDTO> workouts = workoutService.getAllActiveWorkouts();
        return ApiResponse.success(workouts);
    }

    /**
     * 根据 ID 获取健身方案详情
     */
    @GetMapping("/{id}")
    public ApiResponse<WorkoutDTO> getWorkoutById(@PathVariable Long id) {
        System.out.println("获取健身方案详情：" + id);
        try {
            WorkoutDTO workout = workoutService.getWorkoutById(id);
            return ApiResponse.success(workout);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据难度筛选健身方案
     */
    @GetMapping("/difficulty/{difficulty}")
    public ApiResponse<List<WorkoutDTO>> getByDifficulty(@PathVariable String difficulty) {
        System.out.println("按难度筛选：" + difficulty);
        List<WorkoutDTO> workouts = workoutService.getByDifficulty(difficulty);
        return ApiResponse.success(workouts);
    }

    /**
     * 根据目标肌肉筛选
     */
    @GetMapping("/target/{targetMuscle}")
    public ApiResponse<List<WorkoutDTO>> getByTargetMuscle(@PathVariable String targetMuscle) {
        System.out.println("按目标肌肉筛选：" + targetMuscle);
        List<WorkoutDTO> workouts = workoutService.getByTargetMuscle(targetMuscle);
        return ApiResponse.success(workouts);
    }

    /**
     * 更新健身方案（管理员）
     */
    @PutMapping("/{id}")
    public ApiResponse<WorkoutDTO> updateWorkout(@PathVariable Long id, @RequestBody WorkoutDTO request) {
        System.out.println("更新健身方案：" + id);
        try {
            WorkoutDTO updated = workoutService.updateWorkout(id, request);
            return ApiResponse.success("更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除健身方案（管理员）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteWorkout(@PathVariable Long id) {
        System.out.println("删除健身方案：" + id);
        try {
            workoutService.deleteWorkout(id);
            return ApiResponse.success("删除成功", "success");
        } catch (Exception e) {
            return ApiResponse.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 启用/禁用健身方案（管理员）
     */
    @PatchMapping("/{id}/toggle-status")
    public ApiResponse<WorkoutDTO> toggleWorkoutStatus(@PathVariable Long id) {
        System.out.println("切换健身方案状态：" + id);
        try {
            WorkoutDTO updated = workoutService.toggleWorkoutStatus(id);
            return ApiResponse.success("操作成功", updated);
        } catch (Exception e) {
            return ApiResponse.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 上传教程封面
     */
    @PostMapping("/cover/upload")
    public ApiResponse<Map<String, String>> uploadWorkoutCover(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error("上传文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.error("不支持的图片格式，请上传图片文件（JPG/PNG/GIF 等）");
            }

            // 验证文件大小（10MB 以内）
            if (file.getSize() > 10 * 1024 * 1024) {
                return ApiResponse.error("封面图大小不能超过 10MB");
            }

            // 使用 MinioUtil 上传
            String url = minioUtil.uploadImage(file);

            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("filename", file.getOriginalFilename());
            response.put("size", String.valueOf(file.getSize()));

            log.info("教程封面上传成功：{}", url);
            return ApiResponse.success("封面上传成功", response);

        } catch (IllegalArgumentException e) {
            log.error("封面图格式错误：{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("封面图上传失败", e);
            return ApiResponse.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传教程视频
     */
    @PostMapping("/video/upload")
    public ApiResponse<Map<String, String>> uploadWorkoutVideo(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error("上传文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ApiResponse.error("不支持的视频格式，请上传视频文件（MP4/AVI/MOV 等）");
            }

            // 验证文件大小（100MB 以内）
            if (file.getSize() > 100 * 1024 * 1024) {
                return ApiResponse.error("视频大小不能超过 100MB");
            }

            // 使用 MinioUtil 上传
            String url = minioUtil.uploadVideo(file);

            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("filename", file.getOriginalFilename());
            response.put("size", String.valueOf(file.getSize()));

            log.info("教程视频上传成功：{}", url);
            return ApiResponse.success("视频上传成功", response);

        } catch (IllegalArgumentException e) {
            log.error("视频格式错误：{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("视频上传失败", e);
            return ApiResponse.error("上传失败：" + e.getMessage());
        }
    }

}

