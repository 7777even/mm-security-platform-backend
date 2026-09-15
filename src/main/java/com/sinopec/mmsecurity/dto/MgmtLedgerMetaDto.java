package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/**
 * 管理台账元数据：列标题列表与筛选定义。
 */
@Data
public class MgmtLedgerMetaDto {
    /** 域标识（菜单叶子 path 去前导 /） */
    private String domain;
    /** 页面标题 */
    private String title;
    /** 列标题（顺序即渲染顺序） */
    private List<String> columns;
    /** 列筛选定义 */
    private List<MgmtLedgerFilterDto> filters;
}
