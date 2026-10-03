package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.MgmtLedgerListResult;
import com.sinopec.mmsecurity.dto.MgmtLedgerMetaDto;
import com.sinopec.mmsecurity.dto.MgmtLedgerRowWriteRequest;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.MgmtLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * 新增台账行（需 ADMIN 角色）。请求体为按列顺序排列的单元格数组；
     * 行与单元格在同一事务内写入，主键/排序号由服务层显式分配。返回新行主键。
     */
    @PostMapping("/{domain}/rows")
    @RequireAuth(role = "ADMIN")
    public Result<Long> createRow(@PathVariable String domain, @RequestBody MgmtLedgerRowWriteRequest req) {
        return Result.ok(mgmtLedgerService.createRow(domain, req));
    }

    /**
     * 更新台账行（需 ADMIN 角色）。按 rowId 定位（须属于该 domain），
     * 删除旧单元格后按请求重写；刷新数据与删除为台账维护动作，不涉及设备下行。
     */
    @PutMapping("/{domain}/rows/{rowId}")
    @RequireAuth(role = "ADMIN")
    public Result<Void> updateRow(
            @PathVariable String domain,
            @PathVariable Long rowId,
            @RequestBody MgmtLedgerRowWriteRequest req) {
        mgmtLedgerService.updateRow(domain, rowId, req);
        return Result.ok(null);
    }

    /**
     * 删除台账行（需 ADMIN 角色）。同时删除其单元格；行不存在返回 B3 NOT_FOUND。
     */
    @DeleteMapping("/{domain}/rows/{rowId}")
    @RequireAuth(role = "ADMIN")
    public Result<Void> deleteRow(@PathVariable String domain, @PathVariable Long rowId) {
        mgmtLedgerService.deleteRow(domain, rowId);
        return Result.ok(null);
    }
}
