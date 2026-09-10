package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 防区下拉项（GET /system/zones 出参；登录可读，供用户表单「可访问防区」多选）。
 *
 * <p>字段与契约 {@code ZoneItem} 严格一致，供 check-api-contract schema 层对拍（0 漂移）。</p>
 */
@Data
public class ZoneItem implements Serializable {

    private Long id;

    private String zoneCode;

    private String zoneName;

    private Integer sortOrder;

    private Integer status;
}
