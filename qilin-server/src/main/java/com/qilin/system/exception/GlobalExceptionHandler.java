package com.qilin.system.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.qilin.system.common.Result;

import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理类
 * 把异常转换成 Http 响应格式
 * 用于处理系统中所有异常
 * 包括自定义异常和标准异常
 * 例如：InvalidCredentialsException、NullPointerException等
 */
@Slf4j 
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理用户名或密码错误异常
     * @param ex
     * @return 401 错误响应
     * 包含错误信息
     * 例如：用户名或密码错误
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Result<Void>> handleInvalidCredentials(
        InvalidCredentialsException ex) {
            log.error("用户名或密码错误: {}", ex);
            return ResponseEntity.status(401).body(Result.error(401, ex.getMessage()));
    }
    /**
     * 处理请求参数校验异常
     * @param ex
     * @return 400 错误响应
     * 包含错误信息
     * 例如：请求参数不合法
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException ex) {
        var fieldError = ex.getBindingResult().getFieldError();
        String msg = fieldError == null ? "请求参数不合法" : fieldError.getDefaultMessage();
        log.error("请求参数校验异常: {}", ex);
        return ResponseEntity.badRequest().body(Result.error(400, msg));
    }
}
