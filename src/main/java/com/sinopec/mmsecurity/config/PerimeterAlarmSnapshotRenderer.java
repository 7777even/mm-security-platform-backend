package com.sinopec.mmsecurity.config;

import com.sinopec.mmsecurity.entity.FacPerimeterAlarm;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * 周界入侵告警现场抓拍占位图渲染器（仅 dev）。
 *
 * <p>周界告警尚未接真流媒体网关，前端卡片/详情需要一个「由后端传」的现场图。
 * 本组件用 JDK 内置 BufferedImage + ImageIO 现画一张带告警标题/位置/时间的占位 JPEG，
 * 供两处复用：
 * <ul>
 *   <li>{@link PerimeterAlarmSnapshotSeeder}：dev 启动时为存量 snapshot_bytes 为空的告警补图；</li>
 *   <li>{@code SecurityService#createPerimeterAlarm}：手工录入新告警后立即生成，
 *       避免「启动之后新建的告警永远没图」（此前只靠启动期 Seeder，新建告警缩略图空白）。</li>
 * </ul>
 *
 * <p>仅注册于 dev profile；生产环境 bean 不存在，创建流程跳过占位图（后续接真流时
 * 由媒体网关写入实时抓拍，前端 GET /security/perimeter-alarms/{id}/snapshot 字节端点无需改动）。
 */
@Component
@Profile("dev")
public class PerimeterAlarmSnapshotRenderer {

    /** 为单条告警渲染占位抓拍 JPEG 字节；渲染失败由调用方兜底（不阻断告警创建）。 */
    public byte[] render(FacPerimeterAlarm alarm) throws Exception {
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
