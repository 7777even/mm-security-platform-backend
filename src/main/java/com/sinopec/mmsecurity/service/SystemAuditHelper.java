package com.sinopec.mmsecurity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.entity.FacAuditLog;
import com.sinopec.mmsecurity.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 系统管理域服务端审计助手。
 *
 * <p>与既有「前端经 {@code POST /uplink/audit} 上报」并存：本类<b>增补</b>服务端主动落库，
 * 覆盖系统管理域写操作（用户/角色/菜单/字典的增删改与授权、口令重置）。</p>
 *
 * <p><b>约束（对齐 audit-log.md）</b>：</p>
 * <ul>
 *   <li>尽力而为：落库失败仅 warn，绝不阻断主流程（审计是旁路）。</li>
 *   <li>append-only：只 insert，不 update / delete。</li>
 *   <li>脱敏：{@code detail} 严禁包含口令明文或哈希；调用方只传目标 id 与变更字段名。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemAuditHelper {

    private static final String MODULE = "system";

    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 记录一条系统管理域审计（action 形如 {@code system.user.create}）。 */
    public void record(String action, Map<String, Object> detail) {
        try {
            FacAuditLog entry = new FacAuditLog();
            entry.setAction(action);
            entry.setModule(MODULE);
            entry.setDetailJson(detail == null || detail.isEmpty() ? null : objectMapper.writeValueAsString(detail));
            entry.setEventAt(System.currentTimeMillis());
            entry.setCreatedAt(LocalDateTime.now());
            auditLogMapper.insert(entry);
        } catch (Exception ex) {
            log.warn("[system] 审计落库失败，已尽力跳过 action={}", action, ex);
        }
    }
}
