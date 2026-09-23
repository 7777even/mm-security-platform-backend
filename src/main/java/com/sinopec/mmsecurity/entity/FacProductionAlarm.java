package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;

/**
 * 生产应急 - ProductionAlarm 实体（生产报警，对应 H2 表 fac_production_alarm）。
 * facility_id 为空表示全厂级报警；装置区二级页按 facility_id 取本装置区报警并重写 location。
 */
@Data
@TableName(value = "fac_production_alarm")
public class FacProductionAlarm implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long facilityId;

    private String title;

    private String titleColor;

    private String location;

    private String occurredAt;

    private String description;

    private String statusName;

    /** 是否误报：是 / 否 / 未核实（可空）。 */
    private String falseAlarm;
    /** 处置情况文本（可空）。 */
    private String handleResult;
    /** 处置时间（yyyy-MM-dd HH:mm:ss，可空）。 */
    private String handleTime;
    /** 派单人员（多个以英文逗号分隔，可空）。 */
    private String dispatchPersonnel;
    /** 通知方式（APP/SMS，多个以英文逗号分隔，可空）。 */
    private String notifyMethod;
    /** 乐观锁版本列（写回并发防护）。 */
    @Version
    private Long version;

    private Integer iconIndex;

    private String thumb;

    private Integer sortNo;
}
