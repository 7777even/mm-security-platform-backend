package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程填报记录实体（对应表 fac_form_record）。
 *
 * <p>字段范式对齐通讯通知记录域（fac_comm_record）：业务编号 / 类型 / 标题 / 填报人 / 部门 /
 * 时间 / 内容 / 状态 / 备注。id 自增主键，version 乐观锁（MyBatis-Plus @Version）。
 *
 * <p>展示型时间字段 fillAt 以 VARCHAR 原样存放（与 fac_comm_record.occurredAt 一致），不做单位换算。
 * 状态 status 取值：DRAFT 草稿 / SUBMITTED 已提交 / REVIEWED 已审核。
 */
@Data
@TableName("fac_form_record")
public class FacFormRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String formNo;

    private String formType;

    private String title;

    private String reporter;

    private String department;

    private String fillAt;

    /** 结构化填报内容（JSON 字符串，按 formType 维度组织：隐患排查/设备巡检/值班交接/其他）。 */
    private String detailJson;

    private String status;

    private String remark;

    /** 乐观锁版本号：update 时自动比对并自增。 */
    @Version
    private Long version;
}
