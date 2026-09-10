package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 用户分页结果（与前端 PageResult 同构：list/total/page/size）。 */
@Data
public class SystemUserPageResult implements Serializable {

    /** 当前页数据 */
    private List<SystemUserItem> list;

    /** 总记录数 */
    private long total;

    /** 当前页码（1-based） */
    private long page;

    /** 每页大小 */
    private long size;
}
