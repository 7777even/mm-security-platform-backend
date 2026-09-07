package com.sinopec.mmsecurity.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器 — 把异常统一收敛到 Result<T> B3 包络。
 * 前端 unwrapBody<T> 只关心 code/message/data/traceId，按此形态返回。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException ex, HttpServletRequest req) {
        log.warn("[{}] BusinessException code={} msg={}", req.getRequestURI(), ex.getCode(), ex.getMessage());
        // 401/403 映射为真实 HTTP 状态码（与前端契约 Unauthorized/Forbidden 对齐）；
        // 其余业务码（含 2xx 鉴权码）保持 200，由前端按 code 判断。
        HttpStatus status = HttpStatus.OK;
        if (ex.getCode() == 401) status = HttpStatus.UNAUTHORIZED;
        else if (ex.getCode() == 403) status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(Result.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Result<Void>> handleValidation(Exception ex, HttpServletRequest req) {
        String msg = ex instanceof MethodArgumentNotValidException e
                ? e.getBindingResult().getAllErrors().stream()
                    .findFirst().map(err -> err.getDefaultMessage()).orElse("参数校验失败")
                : ((BindException) ex).getAllErrors().stream()
                    .findFirst().map(err -> err.getDefaultMessage()).orElse("参数校验失败");
        log.warn("[{}] Validation failed: {}", req.getRequestURI(), msg);
        return ResponseEntity.ok(Result.fail(ResultCode.PARAM_INVALID, msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("[{}] Illegal arg: {}", req.getRequestURI(), ex.getMessage());
        return ResponseEntity.ok(Result.fail(ResultCode.PARAM_INVALID, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleAny(Exception ex, HttpServletRequest req) {
        log.error("[{}] Unhandled exception", req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(500, "服务器内部错误"));
    }
}
