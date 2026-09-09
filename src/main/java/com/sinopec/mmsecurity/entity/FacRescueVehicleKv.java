package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急救援资源 - 救援车辆键值项（对应 H2 表 fac_rescue_vehicle_kv），按 vehicle_id 关联主表。
 * kv_kind：CONSUMABLE=耗材 / DISPATCH_SUMMARY=出动汇总。
 *
 * <p>注意：列名为 value_text、Java 属性名为 valueText —— H2 中 value 为保留字，
 * 实体属性名不得使用 value（MyBatis-Plus 会拿属性名当 SQL 别名）。
 */
@Data
@TableName(value = "fac_rescue_vehicle_kv")
public class FacRescueVehicleKv implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long vehicleId;

    private String kvKind;

    private String kvLabel;

    private String valueText;

    private Integer sortNo;
}
