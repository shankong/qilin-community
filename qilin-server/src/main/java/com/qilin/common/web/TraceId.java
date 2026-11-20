package com.qilin.common.web;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * 全链路 traceId 工具。
 *
 * <p>请求进入时由 {@link TraceIdFilter} 生成/透传，放进 MDC：
 * 日志模板里用 %X{traceId} 输出，返回体里也带一份。
 * 线上的用法是：用户把页面报错里的 traceId 发给你，你在日志里搜这一个串，
 * 就能看到这次请求打过的所有日志。
 */
public final class TraceId {

    /** MDC 中的 key，和 logback pattern 里的 %X{traceId} 对应 */
    public static final String MDC_KEY = "traceId";

    /** 响应头，前端可以在 Network 面板看到 */
    public static final String HEADER = "X-Trace-Id";

    private TraceId() {
    }

    /** 取当前请求的 traceId；不在请求线程里（如定时任务）时返回空串 */
    public static String current() {
        String traceId = MDC.get(MDC_KEY);
        return traceId == null ? "" : traceId;
    }

    /** 生成 16 位十六进制 traceId，足够短、也足够不重复 */
    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
