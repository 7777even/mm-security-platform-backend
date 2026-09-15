package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 管理台账元数据：每个 domain（菜单叶子 path，去掉前导 /）对应一行，
 * 记录标题、列定义（columns_json）与筛选下拉（filter_json）。
 * 数据由 V51 迁移脚本从 mgmtMenus 静态数据种子化，属只读台账。
 */
@Data
@TableName("mgmt_ledger_meta")
public class MgmtLedgerMeta {
    private Long id;
    private String domain;
    private String title;
    private String columnsJson;
    private String filterJson;
    private Integer sortNo;
}
