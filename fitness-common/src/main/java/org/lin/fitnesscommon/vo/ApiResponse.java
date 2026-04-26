package org.lin.fitnesscommon.vo;

/**
 * @author lin
 * @date 2026-03-24
 */

import lombok.Data;
import org.lin.fitnesscommon.config.MdcUtil;

/**
 * 统一 API 响应封装类
 */
@Data
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;
    private  String traceId;
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(data);
        response.setTraceId(MdcUtil.getTraceId());
        return response;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        response.setTraceId(MdcUtil.getTraceId());
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(500);
        response.setMessage(message);
        response.setTraceId(MdcUtil.getTraceId());
        return response;
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setTraceId(MdcUtil.getTraceId());
        return response;
    }
}
