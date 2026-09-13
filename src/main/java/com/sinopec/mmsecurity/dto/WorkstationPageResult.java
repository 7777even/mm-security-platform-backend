package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/**
 * 工作站分页结果。
 *
 * <p>list 元素复用 {@link Workstation}（与 dashboard.openapi.json#/Workstation 同名对齐）。
 * schema 名与类名一致，确保 {@code check-api-contract.mjs} 精确对拍（同名才真校验，异名一律豁免）。</p>
 */
@Data
public class WorkstationPageResult {
    private List<Workstation> list;
    private long total;
    private long page;
    private long size;
}
