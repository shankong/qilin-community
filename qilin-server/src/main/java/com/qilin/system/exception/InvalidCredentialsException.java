package com.qilin.system.exception;

/**
 * 用户名或密码错误异常
 * 当用户输入的用户名或密码错误时，抛出该异常
 */
public class InvalidCredentialsException extends RuntimeException {
     public InvalidCredentialsException() {
        super("用户名或密码错误");
    }
}
