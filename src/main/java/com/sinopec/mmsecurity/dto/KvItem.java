package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 应急救援资源 - 键值项（车辆耗材 / 出动汇总）。
 *
 * <p>DTO 不参与 SQL，故属性名使用 value 是安全的（实体侧为 valueText）。
 */
@Data
public class KvItem {
    private String label;
    private String value;
}
