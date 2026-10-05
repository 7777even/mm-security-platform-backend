package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

/**
 * 防恐柱实体。数据来自真实表 fac_bollard。
 */
@Data
@TableName("fac_bollard")
public class FacBollard {
    /**
     * 主键。V4__security.sql 中 fac_bollard.id 为 BIGINT PRIMARY KEY（无 AUTO_INCREMENT），
     * 与 fac_gate_control 同一根因：全局 id-type=auto 期望 DB 自增而实际无 → 插入 23502 主键空。
     * 用 ASSIGN_ID 显式生成，跨方言一致。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private String zone;
    private String status;
    private Double longitude;
    private Double latitude;

    /** V101 乐观锁列；status 为设备实时状态，仅读不写（零下行控制红线）。 */
    @Version
    private Long version;
}
