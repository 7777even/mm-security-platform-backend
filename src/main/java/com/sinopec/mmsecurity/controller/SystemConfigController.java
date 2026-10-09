package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.SysConfigNode;
import com.sinopec.mmsecurity.dto.SysConfigSaveRequest;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.SystemConfigService;
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

import java.util.List;

/**
 * 系统参数配置接口（运行期可配项自助管理）。
 *
 * <p>类级 {@code @RequireAuth(role = "ADMIN")} 兜底；方法级 {@code @RequireAuth(perm = "system:config:*")}
 * 为细粒度判定。所有写操作由服务端落 fac_audit_log（module=system）。</p>
 */
@RestController
@RequestMapping("/api/v1/system/configs")
@RequireAuth(role = "ADMIN")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /** 配置列表（group 可选过滤）。 */
    @RequireAuth(perm = "system:config:view")
    @GetMapping
    public Result<List<SysConfigNode>> list(@RequestParam(required = false) String group) {
        return Result.ok(systemConfigService.list(group));
    }

    /** 新增配置。 */
    @RequireAuth(perm = "system:config:create")
    @PostMapping
    public Result<SysConfigNode> create(@Valid @RequestBody SysConfigSaveRequest payload) {
        return Result.ok(systemConfigService.create(payload));
    }

    /** 修改配置。 */
    @RequireAuth(perm = "system:config:edit")
    @PutMapping("/{id}")
    public Result<SysConfigNode> update(@PathVariable Long id, @Valid @RequestBody SysConfigSaveRequest payload) {
        return Result.ok(systemConfigService.update(id, payload));
    }

    /** 删除配置（内置配置拒绝）。 */
    @RequireAuth(perm = "system:config:delete")
    @DeleteMapping("/{id}")
    public Result<DeleteResult> delete(@PathVariable Long id) {
        return Result.ok(systemConfigService.delete(id));
    }
}
