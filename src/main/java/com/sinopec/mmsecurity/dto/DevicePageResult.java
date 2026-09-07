package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.entity.FacDevice;
import lombok.Data;

import java.util.List;

/**
 * 设备分页结果。
 *
 * 字段与前端脚手架 _shared.json 的 {@code PageResult} 完全同构（list/total/page/size），
 * 作为跨库契约对齐的单一形态，避免 Controller 直接返回 Map 导致前端类型丢失。
 */
@Data
public class DevicePageResult {

    /** 当前页数据 */
    private List<FacDevice> list;

    /** 总记录数 */
    private long total;

    /** 当前页码（1-based） */
    private long page;

    /** 每页大小 */
    private long size;
}
