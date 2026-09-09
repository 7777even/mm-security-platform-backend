package com.sinopec.mmsecurity.common;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器 — 把异常统一收敛到 Result<T> B3 包络。
 * 前端 unwrapBody<T> 只关心 code/message/data/traceId，按此形态返回。
 *
 * <p><b>状态码约定（勿随意改动，前端 http.ts 依赖）：</b>
 * <ul>
 *   <li>参数类错误（校验失败 / 请求体解析失败 / 类型不匹配 / 缺参）→ HTTP 200 + code=100，
 *       由前端按 code 判定并展示 message；</li>
 *   <li>鉴权与冲突（401/403/409）→ 真实 HTTP 状态码 + 对应 code；</li>
 *   <li>协议类错误（404/405/415）→ 真实 HTTP 状态码 + 对应 code；</li>
 *   <li>兜底未知异常 → HTTP 500 + code=500，且不外泄内部细节。</li>
 * </ul>
 *
 * <p><b>为什么必须穷举 Web 层异常（2026-09-09 联调阻塞复盘）：</b>
 * 早期本类仅处理 BusinessException / 校验异常 / IllegalArgumentException，
 * 其余全部落进 {@link #handleAny} 兜底返回「500 服务器内部错误」。联调期前后端字段偏差是常态
 * （例如 level 契约为 Integer、前端传字符串 "HIGH"；或批量接口误传数组体），
 * 这类请求本应是 400 参数错误，却一律表现为 500，且 message 被「服务器内部错误」掩盖，
 * 现象即「数据端点全 500」——排查成本极高。故此处对可预期的 Web 层异常逐一映射，
 * 并让 message 指向具体字段与期望类型，使前端无需查看后端日志即可自查偏差。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Jackson 类型 → 面向前端的中文类型名（避免把内部 Java 类名外泄给调用方）。 */
    private static final Map<Class<?>, String> FRIENDLY_TYPES = Map.ofEntries(
            Map.entry(Integer.class, "整数"),
            Map.entry(int.class, "整数"),
            Map.entry(Long.class, "整数"),
            Map.entry(long.class, "整数"),
            Map.entry(Short.class, "整数"),
            Map.entry(Double.class, "小数"),
            Map.entry(double.class, "小数"),
            Map.entry(Float.class, "小数"),
            Map.entry(java.math.BigDecimal.class, "小数"),
            Map.entry(Boolean.class, "布尔值(true/false)"),
            Map.entry(boolean.class, "布尔值(true/false)"),
            Map.entry(String.class, "字符串"),
            Map.entry(java.time.LocalDateTime.class, "日期时间(yyyy-MM-dd HH:mm:ss)"),
            Map.entry(java.time.LocalDate.class, "日期(yyyy-MM-dd)"));

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException ex, HttpServletRequest req) {
        log.warn("[{}] BusinessException code={} msg={}", req.getRequestURI(), ex.getCode(), ex.getMessage());
        // 401/403/409 映射为真实 HTTP 状态码（与前端契约 Unauthorized/Forbidden/Conflict 对齐）；
        // 其余业务码（含 2xx 鉴权码）保持 200，由前端按 code 判断。
        HttpStatus status = HttpStatus.OK;
        if (ex.getCode() == 401) status = HttpStatus.UNAUTHORIZED;
        else if (ex.getCode() == 403) status = HttpStatus.FORBIDDEN;
        else if (ex.getCode() == ResultCode.CONFLICT) status = HttpStatus.CONFLICT;
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

    /**
     * 请求体无法反序列化：JSON 语法错误、字段类型不匹配、对象/数组结构错位等。
     *
     * <p>这是联调期最高频的「伪 500」来源。此处把 Jackson 的类型/路径信息翻译成
     * 「字段 xxx 类型不合法：期望 整数」这类可自查的提示，前端据此即可定位契约偏差。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleNotReadable(HttpMessageNotReadableException ex,
                                                         HttpServletRequest req) {
        String msg = describeParseError(ex);
        // 用 warn 而非 error：这是调用方入参问题，不是服务端故障，不应污染错误告警。
        log.warn("[{}] 请求体解析失败: {} | 原始: {}", req.getRequestURI(), msg, rootMessage(ex));
        return ResponseEntity.ok(Result.fail(ResultCode.PARAM_INVALID, msg));
    }

    /** 路径变量 / 查询参数类型不匹配，如 ?page=abc。 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                          HttpServletRequest req) {
        String expected = ex.getRequiredType() == null ? "合法值"
                : FRIENDLY_TYPES.getOrDefault(ex.getRequiredType(), "合法值");
        String msg = String.format("参数 %s 类型不合法：期望 %s，实际收到 \"%s\"",
                ex.getName(), expected, ex.getValue());
        log.warn("[{}] {}", req.getRequestURI(), msg);
        return ResponseEntity.ok(Result.fail(ResultCode.PARAM_INVALID, msg));
    }

    /** 缺少必填查询参数。 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParam(MissingServletRequestParameterException ex,
                                                          HttpServletRequest req) {
        String msg = String.format("缺少必填参数 %s", ex.getParameterName());
        log.warn("[{}] {}", req.getRequestURI(), msg);
        return ResponseEntity.ok(Result.fail(ResultCode.PARAM_INVALID, msg));
    }

    /** 方法参数级约束校验（@Validated 标注在类上、约束写在方法参数）。 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraint(ConstraintViolationException ex,
                                                         HttpServletRequest req) {
        String msg = ex.getConstraintViolations() == null || ex.getConstraintViolations().isEmpty()
                ? "参数校验失败"
                : ex.getConstraintViolations().stream()
                        .findFirst()
                        .map(v -> {
                            String path = String.valueOf(v.getPropertyPath());
                            // propertyPath 形如 method.argName，仅取末段避免暴露方法名
                            int dot = path.lastIndexOf('.');
                            String field = dot >= 0 ? path.substring(dot + 1) : path;
                            return field + " " + v.getMessage();
                        })
                        .orElse("参数校验失败");
        log.warn("[{}] Constraint violation: {}", req.getRequestURI(), msg);
        return ResponseEntity.ok(Result.fail(ResultCode.PARAM_INVALID, msg));
    }

    /** 请求方法不支持 → 405，避免前端用错动词时看到 500。 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest req) {
        String supported = ex.getSupportedHttpMethods() == null ? "" :
                ex.getSupportedHttpMethods().stream().map(String::valueOf).collect(Collectors.joining("/"));
        String msg = String.format("不支持的请求方法 %s%s", ex.getMethod(),
                supported.isEmpty() ? "" : "，该地址支持 " + supported);
        log.warn("[{}] {}", req.getRequestURI(), msg);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Result.fail(ResultCode.PARAM_INVALID, msg));
    }

    /** Content-Type 不受支持 → 415。 */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMediaType(HttpMediaTypeNotSupportedException ex,
                                                        HttpServletRequest req) {
        String msg = String.format("不支持的 Content-Type：%s，请使用 application/json", ex.getContentType());
        log.warn("[{}] {}", req.getRequestURI(), msg);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(Result.fail(ResultCode.PARAM_INVALID, msg));
    }

    /**
     * 未匹配到任何处理器 → 404。
     *
     * <p>联调价值：前端调用「后端尚未实现」的端点时（例如新域联调未完成），应明确得到 404
     * 而不是 500——前者一眼可判「端点缺失」，后者会被误判为后端故障。
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<Result<Void>> handleNotFound(Exception ex, HttpServletRequest req) {
        String msg = String.format("接口不存在：%s %s", req.getMethod(), req.getRequestURI());
        log.warn("[{}] {}", req.getRequestURI(), msg);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.fail(ResultCode.NOT_FOUND, msg));
    }

    /** 数据库约束冲突（唯一键 / 非空 / 外键）→ 409，语义上属调用方数据冲突而非服务故障。 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> handleDataIntegrity(DataIntegrityViolationException ex,
                                                            HttpServletRequest req) {
        // 不外泄具体约束名/表名/SQL（含库结构信息，属敏感面）；详情仅落日志。
        log.warn("[{}] 数据完整性冲突: {}", req.getRequestURI(), rootMessage(ex));
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Result.fail(ResultCode.CONFLICT, "数据冲突：请检查唯一键或必填字段"));
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

    // ---------------------------------------------------------------- 内部工具

    /**
     * 把 Jackson 解析异常翻译成面向调用方的可自查提示。
     * 只暴露「字段路径 + 期望类型」，绝不外泄目标 Java 类名。
     */
    private String describeParseError(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            String field = fieldPath(ife);
            String expected = FRIENDLY_TYPES.getOrDefault(ife.getTargetType(), "合法值");
            return field.isEmpty()
                    ? String.format("请求体字段类型不合法：期望 %s，实际收到 \"%s\"", expected, ife.getValue())
                    : String.format("请求体字段 %s 类型不合法：期望 %s，实际收到 \"%s\"",
                            field, expected, ife.getValue());
        }
        if (cause instanceof MismatchedInputException mie) {
            String field = fieldPath(mie);
            Class<?> target = mie.getTargetType();
            // 目标是集合却收到对象，或目标是对象却收到数组 —— 批量接口最常踩
            boolean targetIsCollection = target != null
                    && (Collection.class.isAssignableFrom(target) || target.isArray());
            String hint = targetIsCollection ? "期望 JSON 数组" : "期望 JSON 对象";
            return field.isEmpty()
                    ? String.format("请求体结构不合法：%s", hint)
                    : String.format("请求体字段 %s 结构不合法：%s", field, hint);
        }
        return "请求体 JSON 格式不合法，请检查字段类型与结构";
    }

    /** 提取 Jackson 异常的字段路径，如 events[0].action。 */
    private String fieldPath(JsonMappingException ex) {
        if (ex.getPath() == null || ex.getPath().isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (JsonMappingException.Reference ref : ex.getPath()) {
            if (ref.getFieldName() != null) {
                if (sb.length() > 0) sb.append('.');
                sb.append(ref.getFieldName());
            } else if (ref.getIndex() >= 0) {
                sb.append('[').append(ref.getIndex()).append(']');
            }
        }
        return sb.toString();
    }

    /** 取最内层异常消息，仅用于日志。 */
    private String rootMessage(Throwable ex) {
        Throwable cur = ex;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur.getMessage();
    }
}
