package com.qilin.system.common;

import org.slf4j.MDC;

import lombok.Getter;

@Getter
public class Result<T> {
    private final int code;
    private final String msg;
    private final T data;
    private final String traceId;

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.traceId = MDC.get(TraceIdFilter.TRACE_ID);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(String msg) {
        return new Result<>(200, msg, null);
    }

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}