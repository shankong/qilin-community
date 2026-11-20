package com.qilin.common.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 要求当前用户拥有指定角色之一，例如：
 * <pre>
 *   &#64;RequireRole("ADMIN")
 *   &#64;DeleteMapping("/{id}")
 * </pre>
 *
 * <p>角色粒度比权限粗，适合“整块功能只给某类角色”的场景；
 * 按钮/接口级控制优先用 {@link RequirePerm}。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /** 角色编码：ADMIN / MANAGER / WORKER / OWNER */
    String[] value();
}
