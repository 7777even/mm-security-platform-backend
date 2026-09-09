package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 辅助知识库条目，与 typhoon-emergency.openapi.json#/TyphoonAuxItem 对齐。
 */
@Data
public class TyphoonAuxItem implements Serializable {

    /** 条目 id */
    private Long id;
    /** 第一行标题 */
    private String line1;
    /** 第二行标题 */
    private String line2;
    /** 条目数量 */
    private Integer count;
    /** 计数色调 cyan / lime */
    private String countTone;
    /** 图标序号 */
    private Integer iconIndex;
}
