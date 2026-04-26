package org.lin.fitnesscommon.filter;

/**
 * @author lin
 * @date 2026-03-27
 */
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.lin.fitnesscommon.config.MdcUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. 尝试从请求头获取 TraceID（微服务场景）
            String traceId = request.getHeader("X-Trace-ID");

            // 2. 如果没有，则生成新的 TraceID
            MdcUtil.setTraceId(traceId);

            // 3. 将 TraceID 放入响应头，方便前端传递
            response.setHeader("X-Trace-ID", MdcUtil.getTraceId());

            log.debug("请求开始 | TraceID: {} | URI: {} | Method: {}",
                    MdcUtil.getTraceId(), request.getRequestURI(), request.getMethod());

            // 4. 继续执行请求
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("请求处理异常 | TraceID: {} | Error: {}",
                    MdcUtil.getTraceId(), e.getMessage(), e);
            throw e;
        } finally {
            // 5. 清除 MDC（非常重要！防止线程池复用导致污染）
            MdcUtil.clear();
        }
    }
}
