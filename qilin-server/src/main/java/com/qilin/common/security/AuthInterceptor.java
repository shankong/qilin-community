package com.qilin.common.security;

import com.qilin.common.exception.BusinessException;
import com.qilin.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.annotation.Annotation;

/**
 * 登录校验 + 权限校验拦截器（Session 方案的核心）。
 *
 * <p>执行顺序：
 * <ol>
 *   <li>OPTIONS 预检请求直接放行（浏览器预检不带 Cookie，拦了会导致跨域失败）；</li>
 *   <li>从 Session 取 LoginUser，取不到 → 401；</li>
 *   <li>放进 ThreadLocal，供业务代码使用；</li>
 *   <li>检查 @RequireRole / @RequirePerm，不满足 → 403。</li>
 * </ol>
 *
 * <p>和 Spring Security 的关系：本项目没用 Security 的过滤器链，
 * 用拦截器实现同样的“认证 + 授权”，代码量更小、调试直观。
 * 面试被问到时说清楚：Security 更完整（CSRF、方法级安全、OAuth2），
 * 拦截器更轻，适合这种单体的练手项目。
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. CORS 预检请求放行
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        // 非 Controller 方法（静态资源等）不校验
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 2. 取 Session 中的登录用户
        HttpSession session = request.getSession(false);
        Object attribute = session == null ? null : session.getAttribute(LoginUser.SESSION_KEY);
        if (!(attribute instanceof LoginUser loginUser)) {
            // 不要 new Session：未登录请求不应该产生会话
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 3. 放入 ThreadLocal
        LoginUserHolder.set(loginUser);

        // 4. 角色校验
        RequireRole requireRole = findAnnotation(handlerMethod, RequireRole.class);
        if (requireRole != null && !loginUser.hasAnyRole(requireRole.value())) {
            log.warn("越权访问(角色) uri={} user={} need={}", request.getRequestURI(),
                    loginUser.getUsername(), String.join(",", requireRole.value()));
            throw new BusinessException(ResultCode.FORBIDDEN, "该操作需要角色：" + String.join("/", requireRole.value()));
        }

        // 5. 权限校验
        RequirePerm requirePerm = findAnnotation(handlerMethod, RequirePerm.class);
        if (requirePerm != null && !loginUser.hasAnyPerm(requirePerm.value())) {
            log.warn("越权访问(权限) uri={} user={} need={}", request.getRequestURI(),
                    loginUser.getUsername(), String.join(",", requirePerm.value()));
            throw new BusinessException(ResultCode.FORBIDDEN, "缺少权限：" + String.join("/", requirePerm.value()));
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 必须清理，避免线程复用导致身份串号
        LoginUserHolder.clear();
    }

    /** 先找方法上的注解，再找类上的注解 */
    private <A extends Annotation> A findAnnotation(HandlerMethod handlerMethod, Class<A> annotationType) {
        A annotation = handlerMethod.getMethodAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }
        return handlerMethod.getBeanType().getAnnotation(annotationType);
    }
}
