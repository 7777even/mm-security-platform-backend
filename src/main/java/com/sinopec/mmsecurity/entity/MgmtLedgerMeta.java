package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 管理台账元数据：每个 domain（菜单叶子 path，去掉前导 /）对应一行，
 * 记录标题、列定义（columns_json）与筛选下拉（filter_json）。
 * 数据由 V51 迁移脚本从 mgmtMenus 静态数据种子化，属只读台账。
 *
 * 列名 domain_code：`domain` 是达梦 DM8 保留字（[-2007] 语法分析出错），
 * 按仓库规约改列名 + @TableField 映射，Java/对外 JSON 字段名不变。
 */
@Data
@TableName("mgmt_ledger_meta")
public class MgmtLedgerMeta {
    private Long id;
    @TableField("domain_code")
    private String domain;
    private String title;
    private String columnsJson;
    private String filterJson;
    private Integer sortNo;
}
