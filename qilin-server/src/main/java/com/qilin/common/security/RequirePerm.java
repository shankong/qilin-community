package com.qilin.common.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 要求当前用户拥有指定权限之一，例如：
 * <pre>
 *   &#64;RequirePerm("biz:order:dispatch")
 *   &#64;PutMapping("/{id}/dispatch")
 * </pre>
 *
 * <p>权限标识来自 sys_menu.perms，由登录时算出并放进 Session。
 * 校验逻辑在 {@link AuthInterceptor}。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePerm {

    /** 权限标识，写多个时满足任意一个即可 */
    String[] value();
}
