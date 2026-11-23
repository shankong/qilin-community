package com.qilin.system.common;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**    
* 为每个请求添加日志跟踪id
* 用于后续的日志记录
*/
@Component 
public class TraceIdFilter extends OncePerRequestFilter {
    /**
     * 日志跟踪id
     */
    public static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain filterChain)
            throws ServletException, IOException {
                // 生成日志跟踪UUID
                String traceId = UUID.randomUUID().toString().replace("-", "");
                /**
                 * 将日志跟踪id添加到MDC中
                 * 例如：在日志中使用${traceId}替换为实际的traceId
                 * 例如：log.info("traceId: {}", traceId);
                 */
                MDC.put(TRACE_ID, traceId);
                try {
                    /**
                     * 调用过滤器链，继续处理请求
                     */
                    filterChain.doFilter(request, response);
                } finally {
                    /**
                     * 从MDC中移除日志跟踪id
                     * 避免日志记录中包含旧的traceId
                     * web是单线程的，会继承上一个请求的traceId
                     */
                    MDC.remove(TRACE_ID);
                }
    }
    
}
