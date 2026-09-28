package com.sinopec.mmsecurity.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 工业电视录像截图占位图渲染器（仅 dev）。
 *
 * <p>工业电视尚未接真流媒体网关，前端「录像截图采集」面板需要一个「由后端传」的现场图。
 * 本组件用 JDK 内置 BufferedImage + ImageIO 现画一张带厂点名称 / 事件类型 / 时间 / REC 角标的
 * 占位 JPEG，供 {@link TvSnapshotSeeder} 在 dev 启动时为空截图表补样例数据，使无真实设备时
 * 也有截图可看。</p>
 *
 * <p>仅注册于 dev profile；生产环境 bean 不存在，截图表由真实设备上报 / 采集器写入。</p>
 */
@Component
@Profile("dev")
public class TvSnapshotRenderer {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 渲染一张工业电视抓拍占位 JPEG 字节；渲染失败由调用方兜底（不阻断入库）。 */
    public byte[] render(String monitorName, String eventType) throws Exception {
        final int w = 320, h = 180;
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();

        // 背景：深蓝视频监控底色
        g.setColor(new Color(6, 40, 61));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(10, 77, 104));
        g.fillRect(0, 0, w, h / 2);

        // 扫描线 / 网格示意
        g.setColor(new Color(74, 214, 255, 60));
        for (int y = 12; y < h; y += 14) g.drawLine(0, y, w, y);
        g.setColor(new Color(74, 214, 255, 90));
        for (int x = 16; x < w; x += 32) g.drawLine(x, 0, x, h);

        // REC 角标（右上）
        g.setColor(new Color(255, 90, 90));
        g.fillOval(w - 36, 14, 11, 11);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.drawString("REC", w - 22, 24);

        // 监控点位名称
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString(truncate(monitorName, "工业电视"), 14, 42);

        // 事件类型
        g.setColor(new Color(159, 214, 255));
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.drawString("事件：" + nullToEmpty(eventType), 14, 66);

        // 时间戳
        g.setColor(new Color(190, 220, 238));
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.drawString(LocalDateTime.now().format(TS_FMT), 14, h - 16);

        g.dispose();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bi, "jpeg", os);
        return os.toByteArray();
    }

    private String truncate(String s, String fallback) {
        String v = nullToEmpty(s);
        if (v.isEmpty()) v = fallback;
        return v.length() > 16 ? v.substring(0, 16) : v;
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
