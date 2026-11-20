package com.qilin.common.security;

import com.qilin.common.exception.BusinessException;
import com.qilin.common.result.ResultCode;

/**
 * 当前请求的登录用户（ThreadLocal）。
 *
 * <p>为了避免每个 Controller 都写 {@code session.getAttribute(...)}，
 * 拦截器校验通过后把用户放到这里，业务代码用 {@code LoginUserHolder.require()} 直接取。
 *
 * <p>必须在请求结束时清理（见 AuthInterceptor#afterCompletion）：
 * Tomcat 线程池会复用线程，不清理会造成“上一个人的身份被下一个人看到”的事故。
 */
public final class LoginUserHolder {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private LoginUserHolder() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 取当前用户，取不到直接抛 401 */
    public static LoginUser require() {
        LoginUser loginUser = HOLDER.get();
        if (loginUser == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return loginUser;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
