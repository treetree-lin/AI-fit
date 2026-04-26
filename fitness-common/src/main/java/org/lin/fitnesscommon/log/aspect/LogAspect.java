package org.lin.fitnesscommon.log.aspect;

/**
 * @author lin
 * @date 2026-03-25
 */
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.lin.fitnesscommon.config.MdcUtil;
import org.lin.fitnesscommon.log.annotation.Log;
import org.lin.fitnesscommon.log.model.LogRecord;
import org.lin.fitnesscommon.log.service.LogService;
import org.lin.fitnesscommon.log.utils.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.lang.reflect.Method;
import java.time.LocalDateTime;


@Slf4j
@Aspect
@Component
public class LogAspect {

    @Autowired(required = false)
    private LogService logService;

    /**
     * 切点：标注了@Log注解的方法
     */
    @Pointcut("@annotation(org.lin.fitnesscommon.log.annotation.Log)")
    public void logPointCut() {}

    /**
     * 环绕通知
     */
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
String traceId = MdcUtil.getTraceId();
        // 构建日志记录对象
        LogRecord logRecord = buildLogRecord(joinPoint);
        logRecord.setTraceId(traceId);

        Object result = null;
        try {
            // 执行目标方法
            result = joinPoint.proceed();

            // 记录成功日志
            logRecord.setSuccess(true);
            logRecord.setCostTime(System.currentTimeMillis() - startTime);
            if (shouldSaveResult(joinPoint)) {
                logRecord.setResult(JSON.toJSONString(result));
            }

            return result;

        } catch (Exception e) {
            // 记录异常日志
            logRecord.setSuccess(false);
            logRecord.setCostTime(System.currentTimeMillis() - startTime);
            logRecord.setErrorMsg(e.getMessage());
            throw e;

        } finally {
            // 保存日志
            saveLog(logRecord);
        }
    }

    /**
     * 构建日志记录对象
     */
    private LogRecord buildLogRecord(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);

        LogRecord record = new LogRecord();

        record.setOperateTime(LocalDateTime.now());
        record.setClassName(joinPoint.getTarget().getClass().getName());
        record.setMethodName(method.getName());
        record.setDescription(logAnnotation.value());
        record.setOperateType(logAnnotation.type().getDescription());

        // 获取请求参数
        if (logAnnotation.saveParams()) {
            Object[] args = joinPoint.getArgs();
            record.setParams(JSON.toJSONString(args));
        }

        // 获取IP地址
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                record.setIp(IpUtil.getIpAddr(request));
            }
        } catch (Exception e) {

            log.warn("获取IP地址失败: {}", e.getMessage());
        }

        return record;
    }

    /**
     * 判断是否需要保存返回结果
     */
    private boolean shouldSaveResult(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);
        return logAnnotation != null && logAnnotation.saveResult();
    }

    /**
     * 保存日志
     */
    private void saveLog(LogRecord logRecord) {
        if (logService != null) {
            // 异步保存日志
            logService.saveLogAsync(logRecord);
        } else {
            // 如果没有注入LogService，直接打印日志
            log.info("日志记录: {}", JSON.toJSONString(logRecord));
        }
    }
}