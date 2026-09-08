package com.sinopec.mmsecurity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.dto.AuditEvent;
import com.sinopec.mmsecurity.dto.AuditEventBatch;
import com.sinopec.mmsecurity.dto.FieldReportItem;
import com.sinopec.mmsecurity.dto.FieldReportMedia;
import com.sinopec.mmsecurity.entity.FacAuditLog;
import com.sinopec.mmsecurity.entity.FacFieldReport;
import com.sinopec.mmsecurity.mapper.AuditLogMapper;
import com.sinopec.mmsecurity.mapper.FacFieldReportMapper;
import com.sinopec.mmsecurity.security.AuthorizationService;
import com.sinopec.mmsecurity.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 上行数据服务（只监不控）：
 * <ul>
 *   <li>{@link #reportAudit(AuditEventBatch)}：批量审计事件尽力落库 fac_audit_log（D1 C-2 等保二级「安全审计」）。</li>
 *   <li>{@link #submitFieldReport(FieldReportItem)}：防爆手机现场采集回传受理即落库 fac_field_report（204）。</li>
 * </ul>
 * 审计落库为尽力而为：单条失败不影响其余（不阻断业务）。
 * 越权防护：现场回传声明的 reporter 必须是本人（{@link AuthorizationService#assertSelfOrAdmin}），
 * 服务端随后用当前登录态覆盖 reporter（防身份冒用/水平越权）。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UplinkService {

    private final AuditLogMapper auditLogMapper;
    private final AuthorizationService authorizationService;
    private final FacFieldReportMapper facFieldReportMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void reportAudit(AuditEventBatch batch) {
        if (batch == null || batch.getEvents() == null) return;
        for (AuditEvent e : batch.getEvents()) {
            if (e == null) continue;
            try {
                FacAuditLog log = new FacAuditLog();
                log.setAction(e.getAction());
                log.setModule(e.getModule());
                log.setDetailJson(e.getDetail() == null ? null : objectMapper.writeValueAsString(e.getDetail()));
                log.setEventAt(e.getAt());
                log.setCreatedAt(LocalDateTime.now());
                auditLogMapper.insert(log);
            } catch (Exception ex) {
                log.warn("[uplink] 审计事件落库失败，已尽力跳过 action={}", e.getAction(), ex);
            }
        }
    }

    public void submitFieldReport(FieldReportItem item) {
        // 水平越权防护：客户端声明的 reporter 必须是本人（管理员除外），否则 403。
        authorizationService.assertSelfOrAdmin(item.getReporter());
        // 服务端信任身份：覆盖客户端传入的 reporter，杜绝冒用他人身份提交现场回传。
        String actor = UserContext.username();
        item.setReporter(actor);
        // 受理即落库（T7 收口：此前仅确认受理，现已真落库，供应急复盘/核查）。
        // 落库失败抛运行时异常 → 全局处理器返回 500 → 前端离线队列判定失败并重试
        //（204 仅代表真正落库成功，避免「假成功」造成数据静默丢失）。
        FacFieldReport entity = new FacFieldReport();
        entity.setId(item.getId());
        entity.setKind(item.getKind());
        entity.setTitle(item.getTitle());
        entity.setNote(item.getNote());
        entity.setDeviceCode(item.getDeviceCode());
        entity.setMediaJson(serializeMedia(item.getMedia()));
        entity.setCreatedAt(item.getCreatedAt());
        entity.setStatus(item.getStatus());
        entity.setReporter(actor);
        entity.setAttempts(item.getAttempts());
        entity.setLastError(item.getLastError());
        entity.setSyncedAt(item.getSyncedAt());
        try {
            facFieldReportMapper.insert(entity);
            log.info("[uplink] 现场采集回传已落库 id={} kind={} title={} status={} reporter={}",
                    item.getId(), item.getKind(), item.getTitle(), item.getStatus(), actor);
        } catch (RuntimeException ex) {
            log.error("[uplink] 现场采集回传落库失败 id={}", item.getId(), ex);
            throw ex;
        }
    }

    /** 附件媒体序列化（失败降级为 null，不阻断主记录落库） */
    private String serializeMedia(List<FieldReportMedia> media) {
        if (media == null || media.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(media);
        } catch (Exception ex) {
            log.warn("[uplink] 现场回传媒体序列化失败，忽略 mediaJson: {}", ex.getMessage());
            return null;
        }
    }
}
