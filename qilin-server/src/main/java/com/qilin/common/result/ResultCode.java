package com.qilin.common.result;

/**
 * 统一业务状态码。
 *
 * <p>约定：
 * <ul>
 *   <li>200 成功</li>
 *   <li>4xx 与 HTTP 语义保持一致，前端可据此做统一处理（如 401 跳登录）</li>
 *   <li>1000+ 为业务错误（HTTP 仍返回 200），如“工单状态不允许该操作”</li>
 * </ul>
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限执行该操作"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "数据状态冲突，请刷新后重试"),

    SERVER_ERROR(500, "服务器开小差了，请稍后再试"),

    /** 业务规则不满足（HTTP 200 + 业务码） */
    BUSINESS_ERROR(1000, "业务处理失败");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
