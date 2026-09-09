package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 工业电视 - 入厂巡检记录实体（对应 H2 表 fac_tv_inspection_record）。
 * record_kind：VEHICLE=车辆（subject_name 存车牌）/ PERSON=人员（subject_name 存姓名）。
 */
@Data
@TableName(value = "fac_tv_inspection_record")
public class FacTvInspectionRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String recordKind;

    private String areaCode;

    private String subjectName;

    private String badge;

    private String department;

    private String gateName;

    private String recordTime;

    private Integer sortNo;
}
