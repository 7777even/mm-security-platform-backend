package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

/**
 * 道闸（门禁卡口）实体。数据来自真实表 fac_gate_control。
 */
@Data
@TableName("fac_gate_control")
public class FacGateControl {
    /**
     * 主键。V4__security.sql 中 fac_gate_control.id 为 BIGINT PRIMARY KEY（无 AUTO_INCREMENT），
     * 全局 id-type=auto 会期望 DB 自增而 H2/PG/DM 均不会自动生成 → 插入报 23502 主键空。
     * 用 ASSIGN_ID（Java 雪花）显式生成，跨方言行为一致、不依赖 DB 自增特性。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private String location;
    private String status;
    private Double longitude;
    private Double latitude;

    /** V101 乐观锁列；status 为设备实时状态，仅读不写（零下行控制红线）。 */
    @Version
    private Long version;
}
