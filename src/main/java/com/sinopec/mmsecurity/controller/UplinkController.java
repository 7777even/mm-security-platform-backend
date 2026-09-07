package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.AuditEventBatch;
import com.sinopec.mmsecurity.dto.FieldReportItem;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.UplinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * 提交现场采集回传（防爆手机）：前端契约明确「不走 http.ts B3 包络、仅以 HTTP 状态判断成功」，
     * 故返回 {@code 204 No Content}（裸 ResponseEntity，非 Result 包络）。仍受 @RequireAuth 鉴权（401）。
     */
    @PostMapping("/field-reports")
    public ResponseEntity<Void> submitFieldReport(@Valid @RequestBody FieldReportItem item) {
        uplinkService.submitFieldReport(item);
        return ResponseEntity.noContent().build();
    }
}
