package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事故案例库（应急台账，可编辑）。
 * 区别于 {@code fac_alarm(status=3)} 自动归档的只读结案聚合：本表为管理员手工维护的事故案例，
 * 用于复盘与培训。id 由 {@link com.sinopec.mmsecurity.service.LedgerIdSupport} 显式分配
 * （不依赖 DB 自增，避免种子显式插 id 导致的序列滞后撞主键）。
 */
@Data
@TableName("fac_emergency_case")
public class FacEmergencyCase {

    @TableId(type = IdType.INPUT)
    private Long id;

    /** 事故名称（必填）。 */
    private String title;

    /** 事故类型（如 泄漏 / 火灾 / 爆炸）。 */
    private String accidentType;

    /** 事故地点。 */
    private String location;

    /** 发生时间。 */
    private LocalDateTime occurredAt;

    /** 案例摘要。 */
    private String summary;

    /** 经验教训 / 启示。 */
    private String lessons;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
