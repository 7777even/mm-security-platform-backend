-- V26 视频摄像头静态截图（演示）：新增 snapshot_bytes 列，存放后端生成的占位 JPEG。
-- 当前为演示用途，由 dev 启动时的 VideoSnapshotSeeder 写入；后续接真流时
-- 改为媒体网关转发的流地址/实时截图（字节端点 GET /video/cameras/{id}/snapshot 不变）。
ALTER TABLE fac_video_camera ADD COLUMN snapshot_bytes BLOB;
