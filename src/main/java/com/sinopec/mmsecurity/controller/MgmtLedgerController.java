package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.MgmtLedgerListResult;
import com.sinopec.mmsecurity.dto.MgmtLedgerMetaDto;
import com.sinopec.mmsecurity.service.MgmtLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 管理台账通用只读接口。前端后台管理端（apps/mgmt）原由 mgmtMenus 硬编码渲染的静态页，
 * 统一改走本能力：按 domain 取元数据与分页数据，数据已种子化进 mgmt_ledger_* 表。
 * 仅 GET，零下行控制；支持 keyword 模糊搜索与 f_&lt;列名&gt;=值 的按列筛选。
 */
@RestController
@RequestMapping("/api/v1/mgmt-ledger")
@RequiredArgsConstructor
public class MgmtLedgerController {

    private final MgmtLedgerService mgmtLedgerService;

    /** 取某 domain 的元数据（列标题 + 筛选定义）。 */
    @GetMapping("/{domain}/meta")
    public Result<MgmtLedgerMetaDto> meta(@PathVariable String domain) {
        return Result.ok(mgmtLedgerService.meta(domain));
    }

    /** 取某 domain 的分页数据，支持 keyword 与列筛选（f_&lt;列名&gt;=值）。 */
    @GetMapping("/{domain}")
    public Result<MgmtLedgerListResult> list(
            @PathVariable String domain,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam Map<String, String> allParams) {
        Map<String, String> filters = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : allParams.entrySet()) {
            if (e.getKey().startsWith("f_")) {
                filters.put(e.getKey().substring(2), e.getValue());
            }
        }
        return Result.ok(mgmtLedgerService.list(domain, page, size, keyword, filters));
    }
}
