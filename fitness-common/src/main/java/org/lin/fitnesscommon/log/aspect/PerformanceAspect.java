package org.lin.fitnesscommon.log.aspect;

/**
 * @author lin
 * @date 2026-03-25
 */
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PerformanceAspect {

    /**
     * 切点：com.lin.fitness模块下的所有public方法
     */
    @Pointcut("execution(public * org.lin.fitness*..*(..))")
    public void performancePointCut() {}

    /**
     * 慢方法监控
     */
    @Around("performancePointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long costTime = System.currentTimeMillis() - startTime;

        // 超过1秒记录警告
        if (costTime > 1000) {
            log.warn("慢方法: {}.{} 耗时: {}ms",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    costTime);
        }

        return result;
    }
}
