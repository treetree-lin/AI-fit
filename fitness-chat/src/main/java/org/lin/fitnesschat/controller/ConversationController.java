package org.lin.fitnesschat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lin.fitnesscommon.exception.CustomException;
import org.lin.fitnesscommon.entity.User;
import org.lin.fitnessuser.repository.UserRepository;
import org.lin.fitnessuser.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lin
 * @date 2026-04-16
 */

@RestController
@RequestMapping("/api/v1/users/conversation")
public class ConversationController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 查询对话历史，从Redis中获取
     */
    @GetMapping
    public ResponseEntity<?> getConversations(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date) {

        String username = null;
        try {
            // 从token中提取用户名
            username = jwtUtils.extractUsernameFromToken(token.replace("Bearer ", ""));
            if (username == null || username.isEmpty()) {

                throw new CustomException("无效的token", HttpStatus.UNAUTHORIZED);
            }


            // 获取用户信息
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new CustomException("用户不存在", HttpStatus.NOT_FOUND));

            // 尝试不同格式的用户ID来查询Redis
            List<String> possibleUserIds = new ArrayList<>();
            possibleUserIds.add(user.getId().toString());    // 数据库ID（Long转String）
            possibleUserIds.add(username);                 // 用户名
            possibleUserIds.add(String.valueOf(user.getId())); // 另一种数据库ID格式

            // 检查所有Redis键，尝试找到与用户相关的会话ID
            List<String> matchingKeys = new ArrayList<>();
            for (String uId : possibleUserIds) {
                String key = "user:" + uId + ":current_conversation";
                String conversationId = redisTemplate.opsForValue().get(key);
                if (conversationId != null) {
                    matchingKeys.add(key);
                    return getConversationsFromRedis(conversationId, username, start_date, end_date);
                }

            }

            // 无法找到任何对话记录
            // 构建统一响应格式
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取对话历史成功");
            response.put("data", new ArrayList<>());
            return ResponseEntity.ok().body(response);
        } catch (CustomException e) {
            return ResponseEntity.status(e.getStatus()).body(Map.of("code", e.getStatus().value(), "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("code", 500, "message", "服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 从Redis获取对话历史
     */
    private ResponseEntity<?> getConversationsFromRedis(String conversationId, String username, String start_date, String end_date) {
        // 从Redis获取对话历史
        String key = "conversation:" + conversationId;
        String json = redisTemplate.opsForValue().get(key);

        List<Map<String, Object>> formattedConversations = new ArrayList<>();
        if (json != null) {
            try {
                // 将原始Redis数据转换为前端可用的格式
                List<Map<String, String>> history = objectMapper.readValue(json,
                        new TypeReference<List<Map<String, String>>>() {});

                // 解析时间范围
                LocalDateTime startDateTime = null;
                LocalDateTime endDateTime = null;

                if (start_date != null && !start_date.trim().isEmpty()) {
                    try {
                        startDateTime = parseDateTime(start_date);
                    } catch (Exception e) {
                        throw new CustomException("起始时间格式错误: " + start_date, HttpStatus.BAD_REQUEST);
                    }
                }

                if (end_date != null && !end_date.trim().isEmpty()) {
                    try {
                        endDateTime = parseDateTime(end_date);
                    } catch (Exception e) {
                        throw new CustomException("结束时间格式错误: " + end_date, HttpStatus.BAD_REQUEST);
                    }
                }

                // 将对话转换为前端需要的格式，使用存储的时间戳并进行时间过滤
                for (Map<String, String> message : history) {
                    String messageTimestamp = message.getOrDefault("timestamp", "未知时间");

                    // 时间过滤
                    if (startDateTime != null || endDateTime != null) {
                        if (!"未知时间".equals(messageTimestamp)) {
                            try {
                                LocalDateTime messageDateTime = LocalDateTime.parse(messageTimestamp,
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

                                // 检查是否在时间范围内
                                if (startDateTime != null && messageDateTime.isBefore(startDateTime)) {
                                    continue; // 跳过早于起始时间的消息
                                }
                                if (endDateTime != null && messageDateTime.isAfter(endDateTime)) {
                                    continue; // 跳过晚于结束时间的消息
                                }
                            } catch (Exception e) {
                                // 时间戳格式不正确，跳过过滤（包含所有消息）
                            }
                        }
                        // 如果是"未知时间"且设置了时间过滤，跳过该消息
                        else if (startDateTime != null || endDateTime != null) {
                            continue;
                        }
                    }

                    Map<String, Object> messageWithTimestamp = new HashMap<>();
                    messageWithTimestamp.put("role", message.get("role"));
                    messageWithTimestamp.put("content", message.get("content"));
                    messageWithTimestamp.put("timestamp", messageTimestamp);
                    formattedConversations.add(messageWithTimestamp);
                }

            } catch (JsonProcessingException e) {

                throw new CustomException("解析对话历史失败", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {

        }

        // 构建统一响应格式
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取对话历史成功");
        response.put("data", formattedConversations);
        return ResponseEntity.ok().body(response);
    }

    /**
     * 解析日期时间字符串，支持多种格式
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }

        try {
            // 尝试标准格式解析 (2023-01-01T12:00:00)
            return LocalDateTime.parse(dateTimeStr);
        } catch (DateTimeParseException e1) {
            try {
                // 尝试解析不带秒的格式 (2023-01-01T12:00)
                if (dateTimeStr.length() == 16) {
                    return LocalDateTime.parse(dateTimeStr + ":00");
                }

                // 尝试解析不带分钟和秒的格式 (2023-01-01T12)
                if (dateTimeStr.length() == 13) {
                    return LocalDateTime.parse(dateTimeStr + ":00:00");
                }

                // 尝试解析日期格式 (2023-01-01)
                if (dateTimeStr.length() == 10) {
                    return LocalDateTime.parse(dateTimeStr + "T00:00:00");
                }

                // 如果以上都失败，尝试使用自定义格式解析
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                return LocalDateTime.parse(dateTimeStr, formatter);
            } catch (Exception e2) {
                throw new CustomException("无效的日期格式: " + dateTimeStr, HttpStatus.BAD_REQUEST);
            }
        }
    }


}