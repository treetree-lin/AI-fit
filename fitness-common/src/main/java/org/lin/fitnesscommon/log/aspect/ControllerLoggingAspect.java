package org.lin.fitnesscommon.log.aspect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.lin.fitnesscommon.utils.LogUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.UUID;

/**
 * Controller 层统一日志切面：记录入口/出参/异常并统计耗时。
 * 使用场景：拦截项目中所有带 @Controller/@RestController 的方法。
 */
@Aspect
@Component
@Order(0)
public class ControllerLoggingAspect {

    // 切面拦截所有 controller 包下的方法，以及项目中标注了 @Controller/@RestController 的类
    @Around("within(@org.springframework.stereotype.Controller *) || within(@org.springframework.web.bind.annotation.RestController *) || execution(* org.lin..controller..*(..))")
    public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        // 获取请求信息
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        if (attrs instanceof ServletRequestAttributes) {
            ServletRequestAttributes s = (ServletRequestAttributes) attrs;
            request = s.getRequest();
            response = s.getResponse();
        }

        // 构造操作标识
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String operation = className + "." + methodName;

        // 尝试获取用户标识：优先 request attribute "userId"，其次 header "X-User-Id"，否则 null
        String userId = null;
        if (request != null) {
            Object attrUser = request.getAttribute("userId");
            if (attrUser != null) {
                userId = String.valueOf(attrUser);
            } else if (request.getHeader("X-User-Id") != null) {
                userId = request.getHeader("X-User-Id");
            }
        }

        // requestId 支持从 header 获取，否则生成一个
        String requestId = null;
        if (request != null && request.getHeader("X-Request-Id") != null) {
            requestId = request.getHeader("X-Request-Id");
        } else {
            requestId = UUID.randomUUID().toString();
        }

        // 设置上下文，方便 LogUtils 使用 MDC
        LogUtils.setRequestContext(requestId, userId, null);

        // 记录入口日志（包含参数简要）
        String argsStr;
        try {
            argsStr = Arrays.toString(pjp.getArgs());
        } catch (Exception e) {
            argsStr = "[无法序列化参数]";
        }
        LogUtils.logBusiness("ENTRY", userId == null ? "-" : userId, "[入口] %s %s args=%s", operation, (request != null ? (request.getMethod() + " " + request.getRequestURI()) : "-"), argsStr);

        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;
            int status = 0;
            try {
                if (response != null) {
                    status = response.getStatus();
                }
            } catch (Exception ignored) {}

            // 性能日志
            LogUtils.logPerformance(operation, duration, "status=" + status);

            // API 调用日志
            LogUtils.logApiCall(request != null ? request.getMethod() : "-",
                    request != null ? request.getRequestURI() : operation,
                    userId == null ? "-" : userId,
                    status,
                    duration);

            // 记录出口日志（简要）
            LogUtils.logBusiness("EXIT", userId == null ? "-" : userId, "[出口] %s 耗时:%dms", operation, duration);

            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            // 记录错误日志
            LogUtils.logBusinessError("EXCEPTION", userId == null ? "-" : userId, "[异常] %s 耗时:%dms 异常:%s", ex, operation, duration, ex.getMessage());
            // 仍然清理上下文
            throw ex;
        } finally {
            LogUtils.clearRequestContext();
        }
    }
}
