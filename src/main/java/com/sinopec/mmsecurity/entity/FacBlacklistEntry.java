package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 安防黑名单条目实体（对应 H2 表 fac_blacklist_entry）。
 * entry_kind：VEHICLE=车辆黑名单 / PERSON=人员黑名单。
 * subject_name：车辆存车牌号，人员存姓名（脱敏形态）。
 */
@Data
@TableName(value = "fac_blacklist_entry")
public class FacBlacklistEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String entryKind;

    private String subjectName;

    private String idCard;

    private String reasonText;

    private String eventTime;

    private String entryStatus;

    private Integer sortNo;
}
