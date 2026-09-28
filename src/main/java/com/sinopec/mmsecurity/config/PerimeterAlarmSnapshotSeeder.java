package com.sinopec.mmsecurity.config;

import com.sinopec.mmsecurity.entity.FacPerimeterAlarm;
import com.sinopec.mmsecurity.mapper.FacPerimeterAlarmMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 周界入侵告警现场抓拍占位图生成器（仅 dev）。
 *
 * <p>现状：周界告警尚未接真流媒体网关，前端详情/面板需要一个「由后端传」的现场图。
 * 本组件在 dev 启动（Flyway 迁移后）为 snapshot_bytes 为空的存量告警补一张占位 JPEG，
 * 画图逻辑统一在 {@link PerimeterAlarmSnapshotRenderer}（创建流程亦复用，保证新建告警即时有图）。
 *
 * <p>约定：CommandLineRunner（Flyway 之后）+ @Profile("dev")，不污染 prod/dm；
 * 每次启动幂等——已写过抓拍（非 null）的告警跳过。
 *
 * <p>后续接真流：删掉本类与渲染器，snapshot_bytes 改为媒体网关写入的实时抓拍即可，
 * 前端 GET /security/perimeter-alarms/{id}/snapshot 字节端点无需改动。
 */
@Slf4j
@Component
@Profile("dev")
@Order(101)
@RequiredArgsConstructor
public class PerimeterAlarmSnapshotSeeder implements CommandLineRunner {

    private final FacPerimeterAlarmMapper perimeterAlarmMapper;
    private final PerimeterAlarmSnapshotRenderer renderer;

    @Override
    public void run(String... args) {
        List<FacPerimeterAlarm> alarms = perimeterAlarmMapper.selectList(null);
        int generated = 0;
        for (FacPerimeterAlarm alarm : alarms) {
            if (alarm.getSnapshotBytes() != null && alarm.getSnapshotBytes().length > 0) {
                continue;
            }
            try {
                alarm.setSnapshotBytes(renderer.render(alarm));
                perimeterAlarmMapper.updateById(alarm);
                generated++;
            } catch (Exception e) {
                log.warn("[PerimeterAlarmSnapshotSeeder] 生成告警 {} 占位图失败: {}", alarm.getId(),
                        e.getMessage());
            }
        }
        log.info("[PerimeterAlarmSnapshotSeeder] 完成，生成占位图 {} / {} 张", generated, alarms.size());
    }
}
