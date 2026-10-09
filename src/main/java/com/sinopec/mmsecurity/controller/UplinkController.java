package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.AuditEventBatch;
import com.sinopec.mmsecurity.dto.AuditLogItem;
import com.sinopec.mmsecurity.dto.AuditLogPageResult;
import com.sinopec.mmsecurity.dto.FieldReportItem;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.UplinkService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequireAuth
@RequiredArgsConstructor
public class UplinkController {

    private final UplinkService uplinkService;

    /**
     * 上报操作审计事件：B3 包络，客户端忽略响应体。批量尽力落库。
     */
    @PostMapping("/audit/log")
    public Result<Object> reportAudit(@Valid @RequestBody AuditEventBatch batch) {
        uplinkService.reportAudit(batch);
        return Result.ok();
    }

    /**
     * 查询操作审计日志（fac_audit_log，只读）。
     * 后台管理端审计日志页消费；支持按模块 / 动作 / 操作人 / 事件时间范围过滤，登录即可读。
     */
    @GetMapping("/audit/log")
    public Result<AuditLogPageResult> queryAudit(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) Long startAt,
            @RequestParam(required = false) Long endAt) {
        return Result.ok(uplinkService.queryAudit(page, size, module, action, actor, startAt, endAt));
    }

    /**
     * 导出操作审计日志（CSV，UTF-8 BOM，浏览器下载）。
     * 过滤条件与查询接口一致；非 B3 包络，直接返回 text/csv 流。登录即可读（与查询同权）。
     * 导出行上限 {@code EXPORT_LIMIT} 防止超大导出拖垮内存。
     */
    @GetMapping("/audit/log/export")
    public void exportAudit(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) Long startAt,
            @RequestParam(required = false) Long endAt,
            HttpServletResponse response) throws IOException {
        List<AuditLogItem> rows =
                uplinkService.exportAudit(EXPORT_LIMIT, module, action, actor, startAt, endAt);
        writeAuditCsv(response, rows);
    }

    /**
     * 提交现场采集回传（防爆手机）：前端契约明确「不走 http.ts B3 包络、仅以 HTTP 状态判断成功」，
     * 故返回 {@code 204 No Content}（裸 ResponseEntity，非 Result 包络）。仍受 @RequireAuth 鉴权（401）。
     */
    @PostMapping("/field-reports")
    public ResponseEntity<Void> submitFieldReport(@Valid @RequestBody FieldReportItem item) {
        uplinkService.submitFieldReport(item);
        return ResponseEntity.noContent().build();
    }

    /** 单次导出最大行数（防超大导出拖垮内存）。 */
    private static final int EXPORT_LIMIT = 50_000;

    private static final DateTimeFormatter CSV_TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 将审计行写出为 CSV（UTF-8 BOM + 中文表头），便于 Excel 直接打开不乱码。 */
    private void writeAuditCsv(HttpServletResponse response, List<AuditLogItem> rows) throws IOException {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String fileName = "audit-log-" + ts + ".csv";
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        try (PrintWriter w = response.getWriter()) {
            w.write('\uFEFF'); // UTF-8 BOM：Excel 打开中文不乱码
            w.println("ID,操作动作,模块,操作人,事件时间,落库时间,详情");
            for (AuditLogItem r : rows) {
                w.println(
                        csvCell(String.valueOf(r.getId()))
                                + "," + csvCell(r.getAction())
                                + "," + csvCell(r.getModule())
                                + "," + csvCell(r.getActor())
                                + "," + csvCell(formatTs(r.getEventAt()))
                                + "," + csvCell(r.getCreatedAt())
                                + "," + csvCell(r.getDetailJson()));
            }
            w.flush();
        }
    }

    private static String formatTs(Long epochMs) {
        if (epochMs == null) return "";
        return Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).format(CSV_TS);
    }

    /** CSV 单元格转义：含逗号/引号/换行时整体双引号包裹，内部引号翻倍。 */
    private static String csvCell(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
