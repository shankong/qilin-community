package com.qilin.system.exception;

/**
 * 通用的 "业务规则被违反" 异常类
 * 抛出后由 GlobalExceptionHandler 统一处理返回 JSON
 * BusinessException
 */
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
    
}
