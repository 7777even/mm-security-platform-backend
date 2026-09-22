package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.FormRecordCreateRequest;
import com.sinopec.mmsecurity.dto.FormRecordItem;
import com.sinopec.mmsecurity.dto.FormRecordPageResult;
import com.sinopec.mmsecurity.dto.FormRecordUpdateRequest;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.FormRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程填报记录域（mgmt /form 流程填报向导后端化）。
 *
 * <p>契约：docs/api/form-records.openapi.json。读端点仅需登录态；写端点（新增/更新）需 ADMIN 角色。
 * 列表按 id 降序返回，详情未命中返回 B3 NOT_FOUND。
 */
@RestController
@RequestMapping("/api/v1/form-records")
@RequireAuth
@RequiredArgsConstructor
public class FormRecordController {

    private final FormRecordService formRecordService;

    /** 流程填报记录列表（分页，按 id 降序）。 */
    @GetMapping
    public Result<FormRecordPageResult> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(formRecordService.list(page, size));
    }

    /** 流程填报记录详情（未命中返回 B3 404）。 */
    @GetMapping("/{id}")
    public Result<FormRecordItem> detail(@PathVariable Long id) {
        return Result.ok(formRecordService.get(id));
    }

    /** 新增流程填报（任意登录用户即可提交，一线人员填报入口）。 */
    @PostMapping
    public Result<FormRecordItem> create(@RequestBody FormRecordCreateRequest req) {
        return Result.ok(formRecordService.create(req));
    }

    /** 更新流程填报（状态流转等，需 ADMIN 角色）。 */
    @PutMapping("/{id}")
    @RequireAuth(role = "ADMIN")
    public Result<FormRecordItem> update(
            @PathVariable Long id,
            @RequestBody FormRecordUpdateRequest req) {
        return Result.ok(formRecordService.update(id, req));
    }
}
