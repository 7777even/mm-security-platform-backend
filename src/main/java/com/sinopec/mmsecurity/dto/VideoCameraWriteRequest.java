package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 视频摄像头写请求（新建/更新共用）。
 *
 * <p>id 与 sort_no 由服务端按 {@code LedgerIdSupport} 分配（规避 H2/PG/DM 自增序列滞后撞主键）；
 * snapshot_bytes 由 dev seeder 生成，不在写请求中传递。status 取 live/loading/ai。
 */
@Data
public class VideoCameraWriteRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String name;

    private String cameraType;

    private String location;

    private String statusName;

    private Boolean hd;

    private Integer thumbIndex;
}
