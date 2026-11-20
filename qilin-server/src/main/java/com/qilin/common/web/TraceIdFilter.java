package com.qilin.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 为每个请求生成（或透传）traceId，写入 MDC 和响应头。
 *
 * <p>为什么用 Filter 而不是拦截器：
 * Filter 在 DispatcherServlet 之前执行，能覆盖 404、静态资源、参数解析失败等
 * 还没进到 Controller 的情况；拦截器只能覆盖进入 Handler 之后的请求。
 *
 * <p>排序：{@link Ordered#HIGHEST_PRECEDENCE}，尽量早地建立 MDC。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final int MAX_TRACE_ID_LENGTH = 64;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = request.getHeader(TraceId.HEADER);
        // 只接受合法的、长度可控的上游 traceId，避免日志被注入乱七八糟的内容
        if (!StringUtils.hasText(traceId) || traceId.length() > MAX_TRACE_ID_LENGTH) {
            traceId = TraceId.generate();
        }

        MDC.put(TraceId.MDC_KEY, traceId);
        response.setHeader(TraceId.HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            // 线程池会复用线程，必须清理，否则下一个请求会串到上一个 traceId
            MDC.remove(TraceId.MDC_KEY);
        }
    }
}
