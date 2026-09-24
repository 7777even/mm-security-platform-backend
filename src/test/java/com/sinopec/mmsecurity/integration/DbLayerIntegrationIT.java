package com.sinopec.mmsecurity.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.entity.FacAuditLog;
import com.sinopec.mmsecurity.entity.FacFieldReport;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import com.sinopec.mmsecurity.mapper.AuditLogMapper;
import com.sinopec.mmsecurity.mapper.FacFieldReportMapper;
import com.sinopec.mmsecurity.dto.PerimeterAlarmCreateRequest;
import com.sinopec.mmsecurity.dto.PerimeterAlarmDetail;
import com.sinopec.mmsecurity.mapper.FacPerimeterAlarmMapper;
import com.sinopec.mmsecurity.service.SecurityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DB 集成测试层（DB-IT）：启动真实 Spring 上下文 + dev profile（H2 内存库 + Flyway 应用
 * {@code db/migration/h2} 的 V1–V8），在真实数据源上验证 MyBatis-Plus Mapper 的
 * 落库 / 逻辑删除 / 审计写入。
 *
 * <p>与 standalone MockMvc 的 {@code *Test}（答逻辑/契约）分层：本测试答「真实 SQL / 落库 / 逻辑删除」，
 * 且<b>复用 {@code db/migration/h2} 的 V 文件作为唯一 schema 来源，禁止在测试目录复制第二份 DDL</b>。</p>
 *
 * <p>Docker / Testcontainers 不可用（本机无 Docker），故以 H2 充当集成 DB；H2 与 PG / DM 在 Flyway 方言上
 * 不完全等价，生产库语义最终以 {@code docs/deployment/dameng-migration-runbook.md} 在真实实例复核为准。</p>
 */
@SpringBootTest
@ActiveProfiles("dev")
// 钉回内存 H2：dev profile 已改文件库持久化，但集成测试须保持隔离（不碰/不锁 dev 文件库，
// 每次运行都拿到全新 Flyway 种子库，且不与正在运行的 dev 服务争锁）。
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:mm_security_test;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
class DbLayerIntegrationIT {

    @Autowired
    private FacFieldReportMapper fieldReportMapper;
    @Autowired
    private AlarmMapper alarmMapper;
    @Autowired
    private AuditLogMapper auditLogMapper;
    @Autowired
    private FacPerimeterAlarmMapper perimeterAlarmMapper;
    @Autowired
    private SecurityService securityService;

    /** 证明 schema + 种子数据来自 V 文件（非第二份 DDL）：V6 种子行必须可读。 */
    @Test
    void flywaySeededFieldReports_exist() {
        FacFieldReport seed = fieldReportMapper.selectById("r-seed-001");
        assertNotNull(seed, "V6 种子回传应已由 Flyway 落库");
    }

    /** 落库验证：insert 后经主键 select 回得，证明 Mapper 真实写库。 */
    @Test
    void fieldReport_insert_thenSelect_provesPersistence() {
        FacFieldReport r = new FacFieldReport();
        r.setId("it-" + UUID.randomUUID());
        r.setKind("field-report");
        r.setTitle("IT 落库验证");
        r.setStatus("pending");
        fieldReportMapper.insert(r);

        FacFieldReport loaded = fieldReportMapper.selectById(r.getId());
        assertNotNull(loaded);
        assertEquals("IT 落库验证", loaded.getTitle());
    }

    /**
     * 逻辑删除语义：MyBatis-Plus 全局 {@code logic-delete-field: deleted} 已开（application.yml）。
     * 与 AlarmService 口径一致——置 deleted=1 而非物理删除；正常查询（MP 自动追加 deleted=0）随之隐藏该行。
     * 本测试验证「逻辑删除拦截器确实生效」+「置位后查询不可见」。
     */
    @Test
    void alarm_logicalDelete_hidesRowFromQueries() {
        FacAlarm a = new FacAlarm();
        a.setLevel(1);
        a.setTitle("IT 逻辑删除验证");
        a.setOccurredAt(LocalDateTime.now());
        a.setStatus(0);
        a.setDeleted(0);
        alarmMapper.insert(a);
        Long id = a.getId();
        assertNotNull(id, "自增主键应回填");

        // 插入后正常查询可见
        assertNotNull(alarmMapper.selectById(id), "插入后逻辑未删，正常查询应可见");

        // 逻辑删除：deleted 置 1（与 AlarmService 手动 set 口径一致，非物理删除）
        alarmMapper.update(null, new LambdaUpdateWrapper<FacAlarm>()
                .eq(FacAlarm::getId, id).set(FacAlarm::getDeleted, 1));

        // 逻辑删除后，正常查询（MP 自动追加 deleted=0）应隐藏该行
        assertNull(alarmMapper.selectById(id), "逻辑删除后正常查询应隐藏该行（证明拦截器生效）");
    }

    /** 审计落库：与 audit-log.md 设计一致，FacAuditLog 可写、不可变。 */
    @Test
    void auditLog_insert_persists() {
        FacAuditLog log = new FacAuditLog();
        log.setAction("it.test.action");
        log.setModule("db-it");
        log.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(log);
        assertNotNull(auditLogMapper.selectById(log.getId()));
    }

    /**
     * 回归（治安防控-新增周界告警 409）：V29 种子显式写入 id=1,2，但 H2 的 AUTO_INCREMENT 在「显式插入自增列」
     * 时不会推进内部序列，导致首个无 id 的录入 INSERT 取到 id=1 与种子行主键冲突
     * （H2 23505 → DataIntegrityViolationException → 端点返回 409）。V77 已复位序列到 MAX(id)+1。
     * 本测试在「真实 Flyway + H2 种子库」上调用 service 落库，断言连续录入不再主键冲突、且 id 越过种子。
     */
    @Test
    void perimeterAlarm_createAfterSeed_noPrimaryKeyConflict() {
        // 种子应存在 id=1,2
        assertNotNull(perimeterAlarmMapper.selectById(1L), "V29 种子 id=1 应存在");
        assertNotNull(perimeterAlarmMapper.selectById(2L), "V29 种子 id=2 应存在");

        PerimeterAlarmCreateRequest req1 = new PerimeterAlarmCreateRequest();
        req1.setTitle("回归-南门翻越");
        req1.setLocation("厂区南门");
        PerimeterAlarmDetail d1 = securityService.createPerimeterAlarm(req1);
        assertNotNull(d1);
        Long id1 = d1.getId();
        assertTrue(id1 != null && id1 > 2, "首个录入 id 应越过种子（>2），实际=" + id1);

        // 连续第二次录入仍不应主键冲突
        PerimeterAlarmCreateRequest req2 = new PerimeterAlarmCreateRequest();
        req2.setTitle("回归-西门翻越");
        req2.setLocation("厂区西门");
        PerimeterAlarmDetail d2 = securityService.createPerimeterAlarm(req2);
        assertNotNull(d2);
        assertTrue(d2.getId() != null && d2.getId() > id1,
                "连续录入 id 应递增，实际=" + d2.getId());
    }
}
