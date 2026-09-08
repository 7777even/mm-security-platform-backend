package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 人员识别检索结果 DTO —— 与前端 {@code security.openapi.json#/PersonSearchResult} 字节级对齐。
 * 数据来自真实表 fac_person_search。
 */
@Data
public class PersonSearchResult {
    private Long id;
    private String name;
    private String gate;
    private String status;
    private String date;
}
