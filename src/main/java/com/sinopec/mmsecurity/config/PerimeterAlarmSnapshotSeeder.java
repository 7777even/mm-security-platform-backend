package com.sinopec.mmsecurity.config;

import com.sinopec.mmsecurity.entity.FacPerimeterAlarm;
import com.sinopec.mmsecurity.mapper.FacPerimeterAlarmMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * 周界入侵告警现场抓拍占位图生成器（仅 dev）。
 *
 * <p>现状：周界告警尚未接真流媒体网关，前端详情/面板需要一个「由后端传」的现场图。
 * 本组件在 dev 启动（Flyway 迁移后）为 snapshot_bytes 为空的告警用 JDK 内置
 * BufferedImage + ImageIO 现画一张带告警标题/位置/时间的占位 JPEG 写回 BLOB。
 *
 * <p>约定：CommandLineRunner（Flyway 之后）+ @Profile("dev")，不污染 prod/dm；
 * 每次启动幂等——已写过抓拍（非 null）的告警跳过。
 *
 * <p>后续接真流：删掉本类，snapshot_bytes 改为媒体网关写入的实时抓拍即可，
 * 前端 GET /security/perimeter-alarms/{id}/snapshot 字节端点无需改动。
 */
@Slf4j
@Component
@Profile("dev")
@Order(101)
@RequiredArgsConstructor
public class PerimeterAlarmSnapshotSeeder implements CommandLineRunner {

    private final FacPerimeterAlarmMapper perimeterAlarmMapper;

    @Override
    public void run(String... args) {
        List<FacPerimeterAlarm> alarms = perimeterAlarmMapper.selectList(null);
        int generated = 0;
        for (FacPerimeterAlarm alarm : alarms) {
            if (alarm.getSnapshotBytes() != null && alarm.getSnapshotBytes().length > 0) {
                continue;
            }
            try {
                alarm.setSnapshotBytes(renderPlaceholder(alarm));
                perimeterAlarmMapper.updateById(alarm);
                generated++;
            } catch (Exception e) {
                log.warn("[PerimeterAlarmSnapshotSeeder] 生成告警 {} 占位图失败: {}", alarm.getId(),
                        e.getMessage());
            }
        }
        log.info("[PerimeterAlarmSnapshotSeeder] 完成，生成占位图 {} / {} 张", generated, alarms.size());
    }

    private byte[] renderPlaceholder(FacPerimeterAlarm alarm) throws Exception {
        final int w = 640, h = 360;
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();

        // 背景：夜间红外监控底色
        g.setColor(new Color(12, 26, 34));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(18, 40, 52));
        g.fillRect(0, h - 74, w, 74);

        // 围栏示意：竖向栏杆 + 横向拉丝
        g.setColor(new Color(120, 150, 165));
        for (int x = 40; x < w; x += 46) g.fillRect(x, 90, 5, 170);
        g.setColor(new Color(90, 118, 132));
        for (int y = 104; y < 260; y += 34) g.drawLine(30, y, w - 30, y);

        // 入侵目标框（红）
        g.setColor(new Color(232, 68, 72));
        g.drawRect(196, 112, 116, 150);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString("INTRUSION", 200, 106);

        // 文本区
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        g.drawString(nullToEmpty(alarm.getTitle()), 30, 44);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
        g.setColor(new Color(190, 220, 238));
        g.drawString("位置：" + nullToEmpty(alarm.getLocation()), 30, h - 46);
        g.drawString("设备：" + nullToEmpty(alarm.getDeviceId()) + "   时间："
                + nullToEmpty(alarm.getAlarmTime()), 30, h - 22);

        // 右下角状态角标
        String status = nullToEmpty(alarm.getStatus());
        g.setColor(new Color(196, 54, 58));
        g.fillRect(w - 132, 22, 108, 34);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        g.drawString(status, w - 120, 45);

        g.dispose();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bi, "jpeg", os);
        return os.toByteArray();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
