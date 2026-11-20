package com.qilin.common.exception;

import com.qilin.common.result.ResultCode;

/**
 * 业务异常。
 *
 * <p>用法：{@code throw new BusinessException("工单已派单，不能重复派单");}
 *
 * <p>抛出后由 {@link GlobalExceptionHandler} 统一转成
 * {@code { code, msg, data, traceId }}，并打日志（warn 级别，不打印堆栈）。
 *
 * <p>为什么不用 {@code RuntimeException} 直接抛：业务异常是“预期内”的错误，
 * 需要区分“用户操作不合法”和“系统真的出 bug 了”，后者才应该 error 级别 + 堆栈。
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 业务状态码，默认 1000（HTTP 仍返回 200） */
    private final int code;

    public BusinessException(String message) {
        this(ResultCode.BUSINESS_ERROR.getCode(), message);
    }

    public BusinessException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMsg());
    }

    public BusinessException(ResultCode resultCode, String message) {
        this(resultCode.getCode(), message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
