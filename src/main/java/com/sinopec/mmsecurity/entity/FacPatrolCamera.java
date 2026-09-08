package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 巡逻摄像机实体。数据来自真实表 fac_patrol_camera（由 FacPatrolCameraMapper 查询），
 * 不再返回前端本地 const 占位数据。
 */
@Data
@TableName("fac_patrol_camera")
public class FacPatrolCamera {
    private Long id;
    private String name;
    private String zone;
    private String status;
    private Double longitude;
    private Double latitude;
}
