package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 工位/工作站 DTO —— 与前端脚手架 {@code dashboard.openapi.json#/Workstation} 字节级对齐。
 *
 * 字段：id / name / zone / online。id 由 fac_workstation.workstation_id 映射，
 * 数据来自真实工位主数据表（由 {@code FacWorkstationMapper} 查询），不返回硬编码/随机值。
 */
@Data
public class Workstation {
    private String id;
    private String name;
    private String zone;
    private boolean online;
}
