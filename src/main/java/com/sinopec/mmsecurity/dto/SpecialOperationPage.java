package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 特殊作业 - 作业票分页（对应前端 specialOperationItems + 分页语义）。 */
@Data
public class SpecialOperationPage {
    private Long total;
    private Integer page;
    private Integer size;
    private Integer pages;
    private java.util.List<SpecialOperationItem> list;
}
