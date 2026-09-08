package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

/**
 * 现场采集回传落库表（防爆手机离线队列上行）。UplinkService.submitFieldReport 受理即落库。
 * id 由客户端生成（UUID），故用 INPUT（业务侧显式赋值，非自增）。
 */
@Data
@TableName("fac_field_report")
public class FacFieldReport {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 乐观锁版本号：MyBatis-Plus @Version，在 update 时自动比对并自增。 */
    @Version
    private Long version;

    private String kind;
    private String title;
    private String note;
    private String deviceCode;

    /** 附件媒体 JSON 列表（FieldReportMedia 序列化存储） */
    @TableField("media_json")
    private String mediaJson;

    private Long createdAt;
    private String status;
    private String reporter;
    private Integer attempts;
    private String lastError;
    private Long syncedAt;
}
