package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 字典项分页结果（与前端 PageResult 同构）。 */
@Data
public class DictItemPageResult implements Serializable {

    /** 当前页数据 */
    private List<DictItemItem> list;

    /** 总记录数 */
    private long total;

    /** 当前页码（1-based） */
    private long page;

    /** 每页大小 */
    private long size;
}
