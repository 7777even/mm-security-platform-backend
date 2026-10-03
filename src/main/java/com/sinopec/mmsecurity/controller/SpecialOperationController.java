package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.SpecialOperationDetail;
import com.sinopec.mmsecurity.dto.SpecialOperationItem;
import com.sinopec.mmsecurity.dto.SpecialOperationPage;
import com.sinopec.mmsecurity.dto.SpecialOperationWriteRequest;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.SpecialOperationService;
import jakarta.validation.Valid;
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

/** 特殊作业大屏只读接口，数据源为 V16 fac_special_operation_* 真实表。 */
@RestController
@RequestMapping("/api/v1/special-operations")
@RequiredArgsConstructor
public class SpecialOperationController {

    private final SpecialOperationService specialOperationService;

    /** 作业票分页（支持类型/区域/等级/状态筛选，「全部xx」按不过滤处理）。 */
    @GetMapping
    public Result<SpecialOperationPage> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status) {
        return Result.ok(specialOperationService.list(page, size, type, area, level, status));
    }

    /** 作业票详情（含现场视频/气体检测点/作业人员）；未命中返回 404 业务码。 */
    @GetMapping("/{id}")
    public Result<SpecialOperationDetail> detail(@PathVariable Long id) {
        SpecialOperationDetail detail = specialOperationService.detail(id);
        if (detail == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "作业票不存在：" + id);
        }
        return Result.ok(detail);
    }

    /** 新建作业票（id 与 sort_no 由服务端分配）；只写主票表。 */
    @PostMapping
    @RequireAuth(perm = "special-operation:write")
    public Result<SpecialOperationItem> create(@Valid @RequestBody SpecialOperationWriteRequest payload) {
        return Result.ok(specialOperationService.createTicket(payload));
    }

    /** 更新作业票（按 id）；未命中 data 为 null。 */
    @PutMapping("/{id}")
    @RequireAuth(perm = "special-operation:write")
    public Result<SpecialOperationItem> update(@PathVariable Long id,
                                               @Valid @RequestBody SpecialOperationWriteRequest payload) {
        return Result.ok(specialOperationService.updateTicket(id, payload));
    }

    /** 删除作业票（按 id）；未命中 ok=false。 */
    @DeleteMapping("/{id}")
    @RequireAuth(perm = "special-operation:write")
    public Result<DeleteResult> delete(@PathVariable Long id) {
        return Result.ok(specialOperationService.deleteTicket(id));
    }
}
