package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应急知识库条目写请求（新增 / 编辑共用）。字段名对齐只读 DTO {@link KnowledgeItem}，
 * 前端 {@code openEdit} 可直接 {@code {...row}} 灌入表单，零字段映射。
 */
@Data
public class KnowledgeWriteRequest implements Serializable {

    /** 知识标题（必填，对应表内 title 列）。 */
    private String title;

    /** 知识条目数（可选）。 */
    private Integer count;

    /** 图标名（Element Plus icon 名，可选）。 */
    private String icon;

    /** 知识分类说明（真实可编辑文案，可选）。 */
    private String description;
}
