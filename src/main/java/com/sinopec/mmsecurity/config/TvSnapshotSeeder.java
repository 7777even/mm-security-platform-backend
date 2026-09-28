package com.sinopec.mmsecurity.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.entity.FacTvMonitor;
import com.sinopec.mmsecurity.entity.FacTvSnapshot;
import com.sinopec.mmsecurity.mapper.FacTvMonitorMapper;
import com.sinopec.mmsecurity.mapper.FacTvSnapshotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 工业电视录像截图样例数据生成器（仅 dev）。
 *
 * <p>现状：工业电视尚未接真流媒体网关 / 采集器默认关闭，fac_tv_snapshot 初始为空，
 * 前端「录像截图采集」面板无数据可看。本组件在 dev 启动（Flyway 之后）若截图表为空，
 * 取若干 fac_tv_monitor 点位生成样例截图行（带占位图），使无真实设备时也有截图可看。
 *
 * <p>约定：CommandLineRunner（Flyway 之后）+ @Profile("dev")，不污染 prod/dm；
 * 幂等——截图表已有数据则跳过，避免每次启动重复插入。</p>
 */
@Slf4j
@Component
@Profile("dev")
@Order(101)
@RequiredArgsConstructor
public class TvSnapshotSeeder implements CommandLineRunner {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] EVENTS = {"烟火检测", "区域入侵", "人员闯入", "手动抓拍"};

    private final FacTvSnapshotMapper snapshotMapper;
    private final FacTvMonitorMapper monitorMapper;
    private final TvSnapshotRenderer renderer;

    @Override
    public void run(String... args) {
        Long existing = snapshotMapper.selectCount(null);
        if (existing != null && existing > 0) {
            log.info("[TvSnapshotSeeder] 截图表已有 {} 行，跳过样例生成", existing);
            return;
        }
        List<FacTvMonitor> monitors = monitorMapper.selectList(null);
        if (monitors == null || monitors.isEmpty()) {
            log.info("[TvSnapshotSeeder] 无 fac_tv_monitor 点位，跳过样例生成");
            return;
        }
        int generated = 0;
        int i = 0;
        for (FacTvMonitor m : monitors.stream().limit(6).toList()) {
            String event = EVENTS[i % EVENTS.length];
            try {
                FacTvSnapshot e = new FacTvSnapshot();
                e.setMonitorCode(m.getMonitorCode());
                e.setMonitorName(m.getMonitorName());
                e.setCaptureTime(LocalDateTime.now().minusMinutes((long) i * 7).format(TS_FMT));
                e.setEventType(event);
                e.setReviewStatus(i % 3 == 0 ? "ACKED" : "PENDING");
                e.setSource("DEVICE");
                e.setSnapshotBytes(renderer.render(m.getMonitorName(), event));
                e.setCreatedAt(LocalDateTime.now().format(TS_FMT));
                e.setSortNo(0);
                snapshotMapper.insert(e);
                generated++;
            } catch (Exception ex) {
                log.warn("[TvSnapshotSeeder] 生成点位 {} 样例截图失败: {}", m.getMonitorCode(), ex.getMessage());
            }
            i++;
        }
        log.info("[TvSnapshotSeeder] 完成，生成样例录像截图 {} 张", generated);
    }
}
