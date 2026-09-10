-- V26 视频摄像头静态截图（演示）：新增 snapshot_bytes 列（迁移资产，DM8 方言）。
-- 与 H2 V26 对齐；dev 由 VideoSnapshotSeeder 写入占位图，生产接真流后由媒体网关写入。
-- 注意：本 dameng 迁移目录为迁移资产（本机无 DM8 实例、未实跑），全量重建时需补齐 V4–V25。
ALTER TABLE fac_video_camera ADD COLUMN snapshot_bytes BLOB;
