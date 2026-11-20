package com.qilin.common.exception;

import com.qilin.common.result.Result;
import com.qilin.common.result.ResultCode;
import com.qilin.common.web.TraceId;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理：所有异常统一转成 {@code { code, msg, data, traceId }}。
 *
 * <p>设计约定（面试可讲）：
 * <ul>
 *   <li>业务异常（预期内，如“工单状态不允许”）：warn 日志、不打堆栈；
 *       如果带的是 401/403/400 这类 HTTP 语义码，就同时设置对应 HTTP 状态；</li>
 *   <li>业务规则错误码（1000+）：HTTP 200 + 业务码，前端按 code 提示；</li>
 *   <li>未捕获异常（预期外，真 bug）：error 日志 + 堆栈，HTTP 500，返回文案不暴露内部细节。</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：最常见的“用户操作不合法” */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常 code={} msg={} traceId={}", e.getCode(), e.getMessage(), TraceId.current());
        HttpStatus status = HttpStatus.resolve(e.getCode());
        if (status == null || !status.isError()) {
            // 1000+ 的业务码：HTTP 仍然 200，由前端读 body.code 处理
            status = HttpStatus.OK;
        }
        return ResponseEntity.status(status).body(Result.fail(e.getCode(), e.getMessage()));
    }

    /** @Valid 校验失败（RequestBody 上的 DTO 校验） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        if (msg.isBlank()) {
            msg = ResultCode.BAD_REQUEST.getMsg();
        }
        log.warn("参数校验失败: {} traceId={}", msg, TraceId.current());
        return ResponseEntity.badRequest().body(Result.fail(ResultCode.BAD_REQUEST, msg));
    }

    /** @Validated 校验失败（方法参数上的约束，如 @NotBlank @RequestParam） */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败: {} traceId={}", msg, TraceId.current());
        return ResponseEntity.badRequest().body(Result.fail(ResultCode.BAD_REQUEST, msg));
    }

    /** 缺少必填请求参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParameter(MissingServletRequestParameterException e) {
        return badRequest("缺少必填参数：" + e.getParameterName());
    }

    /** 参数类型不匹配，如把 abc 传给 Long */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return badRequest("参数类型不正确：" + e.getName());
    }

    /** 请求体不是合法 JSON */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {} traceId={}", e.getMessage(), TraceId.current());
        return badRequest("请求体格式不正确，请检查 JSON 与字段类型");
    }

    /** 上传文件超过 spring.servlet.multipart.max-file-size */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result<Void>> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return badRequest("上传文件过大，请压缩后再试");
    }

    /** HTTP 方法不支持 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Result.fail(ResultCode.METHOD_NOT_ALLOWED));
    }

    /** 路径不存在（Spring 6.1 静态资源找不到时抛这个） */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResource(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.fail(ResultCode.NOT_FOUND, "接口不存在：" + e.getResourcePath()));
    }

    /** 路径不存在（开启 throw-exception-if-no-handler-found 时抛这个） */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNoHandler(NoHandlerFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.fail(ResultCode.NOT_FOUND));
    }

    /**
     * 唯一索引冲突（如账单 (house_id, period) 重复生成）。
     * 这是“数据库兜底”的最后一层：应用层判重之外，唯一索引保证并发下也不会重复。
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Void>> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("唯一索引冲突: {} traceId={}", e.getMessage(), TraceId.current());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Result.fail(ResultCode.CONFLICT, "数据已存在，请勿重复提交"));
    }

    /** 兜底：预期外的异常，必须打堆栈 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("系统异常 traceId={}", TraceId.current(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(ResultCode.SERVER_ERROR));
    }

    private ResponseEntity<Result<Void>> badRequest(String msg) {
        log.warn("请求参数错误: {} traceId={}", msg, TraceId.current());
        return ResponseEntity.badRequest().body(Result.fail(ResultCode.BAD_REQUEST, msg));
    }
}
