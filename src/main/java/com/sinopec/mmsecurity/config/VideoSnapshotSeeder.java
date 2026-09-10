package com.sinopec.mmsecurity.config;

import com.sinopec.mmsecurity.entity.FacVideoCamera;
import com.sinopec.mmsecurity.mapper.FacVideoCameraMapper;
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
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * 视频摄像头静态截图占位图生成器（仅 dev）。
 *
 * <p>现状：视频域尚未接真流，前端格子需展示「由后端传」的静态图。
 * 本组件在 dev 启动（Flyway 建表后）为 snapshot_bytes 为空的摄像头用 JDK 内置
 * BufferedImage + ImageIO 现画一张带名称/位置/REC 角标的占位 JPEG 写回 BLOB。
 *
 * <p>约定：跑在 CommandLineRunner（Spring 上下文刷新完成、Flyway 迁移之后），且 @Profile("dev")，
 * 不污染 prod/dm。每次启动幂等——已写过截图（非 null）的摄像头跳过。
 *
 * <p>后续接真流时：删掉本类、把 snapshot_bytes 改为媒体网关写入的实时截图/流首帧即可，
 * 前端 GET /video/cameras/{id}/snapshot 字节端点无需改动。
 */
@Slf4j
@Component
@Profile("dev")
@Order(100)
@RequiredArgsConstructor
public class VideoSnapshotSeeder implements CommandLineRunner {

    private final FacVideoCameraMapper cameraMapper;

    @Override
    public void run(String... args) {
        if (GraphicsEnvironment.isHeadless()) {
            log.info("[VideoSnapshotSeeder] headless 环境，占位图生成依赖 ImageIO（无需显示设备），继续。");
        }
        List<FacVideoCamera> cameras = cameraMapper.selectList(null);
        int generated = 0;
        for (FacVideoCamera camera : cameras) {
            if (camera.getSnapshotBytes() != null && camera.getSnapshotBytes().length > 0) {
                continue;
            }
            try {
                byte[] img = renderPlaceholder(camera);
                camera.setSnapshotBytes(img);
                cameraMapper.updateById(camera);
                generated++;
            } catch (Exception e) {
                log.warn("[VideoSnapshotSeeder] 生成摄像头 {} 占位图失败: {}", camera.getId(), e.getMessage());
            }
        }
        log.info("[VideoSnapshotSeeder] 完成，生成占位图 {} / {} 张", generated, cameras.size());
    }

    private byte[] renderPlaceholder(FacVideoCamera camera) throws Exception {
        final int w = 640, h = 360;
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();

        // 背景：按 id 取色相，营造不同摄像头不同底色
        float hue = ((camera.getId() == null ? 0L : camera.getId()) % 12) * 30f / 360f;
        Color base = Color.getHSBColor(hue, 0.45f, 0.30f);
        g.setColor(base);
        g.fillRect(0, 0, w, h);
        g.setColor(base.darker());
        g.fillRect(0, h - 60, w, 60);

        // 模拟监控画面的细网格
        g.setColor(new Color(255, 255, 255, 28));
        for (int x = 0; x <= w; x += 40) g.drawLine(x, 0, x, h);
        for (int y = 0; y <= h; y += 40) g.drawLine(0, y, w, y);

        // 摄像头名称
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        drawStringSafe(g, camera.getName(), 30, 62);
        // 安装位置
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        g.setColor(new Color(200, 230, 255));
        drawStringSafe(g, camera.getLocation(), 30, 96);

        // 右上 REC 角标
        g.setColor(new Color(220, 60, 60));
        g.fillRect(w - 122, 24, 96, 36);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        g.drawString("● REC", w - 114, 50);

        // 底部水印
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        g.setColor(new Color(255, 255, 255, 150));
        g.drawString("静态图（演示）· 待接真流", 30, h - 24);

        g.dispose();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bi, "jpeg", os);
        return os.toByteArray();
    }

    private void drawStringSafe(Graphics2D g, String s, int x, int y) {
        g.drawString(s == null ? "" : s, x, y);
    }
}
