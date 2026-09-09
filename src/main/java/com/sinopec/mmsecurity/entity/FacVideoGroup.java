package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 视频控制 - 视频分组节点实体（对应 H2 表 fac_video_group）。
 * group_kind：CATEGORY=顶部分类（含图标），TREE=分组树节点；parent_code 为空即根节点。
 */
@Data
@TableName(value = "fac_video_group")
public class FacVideoGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 前端字符串节点编码（refining / drill-1 ...），即契约里的 id */
    private String nodeCode;

    private String label;

    /** 父节点编码；根节点为 null */
    private String parentCode;

    /** CATEGORY / TREE */
    private String groupKind;

    private Integer iconIndex;

    private Integer sortNo;
}
