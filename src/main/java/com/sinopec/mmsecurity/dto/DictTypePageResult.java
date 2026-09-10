package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 字典类型分页结果（与前端 PageResult 同构）。 */
@Data
public class DictTypePageResult implements Serializable {

    /** 当前页数据 */
    private List<DictTypeItem> list;

    /** 总记录数 */
    private long total;

    /** 当前页码（1-based） */
    private long page;

    /** 每页大小 */
    private long size;
}
