package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 通讯通知记录实体（对应 H2 表 fac_comm_record）。
 *
 * <p>一张表承载五类通讯记录，以 record_type 区分：sms=短信 / call=电话通话 / broadcast=广播播报 /
 * push=APP推送 / intercom=语音对讲。五类记录字段语义同构（谁、何时、经何通道、对谁、内容、结果），
 * 差异项收敛到 channel / direction / contentType 三个附加列。
 *
 * <p>展示型时间字段 occurredAt 以 VARCHAR 原样存放（与 fac_comm_device 一致）。
 * 列名 result_text 而非 result：回避各方言可能的保留字冲突，Java 字段仍为 result。
 */
@Data
@TableName(value = "fac_comm_record")
public class FacCommRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String recordNo;

    private String recordType;

    private String occurredAt;

    private String category;

    private String sender;

    private String receiver;

    private String summary;

    @TableField("result_text")
    private String result;

    private String duration;

    private String channel;

    private String direction;

    private String contentType;
}
