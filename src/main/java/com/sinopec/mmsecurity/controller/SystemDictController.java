package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.DictItemItem;
import com.sinopec.mmsecurity.dto.DictItemPageResult;
import com.sinopec.mmsecurity.dto.DictItemSaveRequest;
import com.sinopec.mmsecurity.dto.DictTypeItem;
import com.sinopec.mmsecurity.dto.DictTypePageResult;
import com.sinopec.mmsecurity.dto.DictTypeSaveRequest;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.SystemDictService;
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
 * 数据字典管理接口。
 *
 * <p>类级门禁为 ADMIN（字典维护属系统管理域）；唯一例外是
 * {@link #options(String)}——业务只读端点，任何已登录用户可用（方法级注解覆盖类级）。</p>
 */
@RestController
@RequestMapping("/api/v1/system")
@RequireAuth(role = "ADMIN")
@RequiredArgsConstructor
public class SystemDictController {

    private final SystemDictService systemDictService;

    /** 字典类型分页。 */
    @GetMapping("/dict-types")
    public Result<DictTypePageResult> typePage(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(systemDictService.typePage(page, size, keyword));
    }

    /** 新增字典类型。 */
    @PostMapping("/dict-types")
    public Result<DictTypeItem> createType(@Valid @RequestBody DictTypeSaveRequest payload) {
        return Result.ok(systemDictService.createType(payload));
    }

    /** 修改字典类型（内置字典禁改标识；改标识时同步迁移字典项）。 */
    @PutMapping("/dict-types/{id}")
    public Result<DictTypeItem> updateType(@PathVariable Long id, @Valid @RequestBody DictTypeSaveRequest payload) {
        return Result.ok(systemDictService.updateType(id, payload));
    }

    /** 删除字典类型（内置禁删、有字典项禁删）。 */
    @DeleteMapping("/dict-types/{id}")
    public Result<DeleteResult> deleteType(@PathVariable Long id) {
        return Result.ok(systemDictService.deleteType(id));
    }

    /** 字典项分页（须指定 dictCode）。 */
    @GetMapping("/dict-items")
    public Result<DictItemPageResult> itemPage(
            @RequestParam String dictCode,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        return Result.ok(systemDictService.itemPage(page, size, dictCode));
    }

    /** 新增字典项。 */
    @PostMapping("/dict-items")
    public Result<DictItemItem> createItem(@Valid @RequestBody DictItemSaveRequest payload) {
        return Result.ok(systemDictService.createItem(payload));
    }

    /** 修改字典项。 */
    @PutMapping("/dict-items/{id}")
    public Result<DictItemItem> updateItem(@PathVariable Long id, @Valid @RequestBody DictItemSaveRequest payload) {
        return Result.ok(systemDictService.updateItem(id, payload));
    }

    /** 删除字典项。 */
    @DeleteMapping("/dict-items/{id}")
    public Result<DeleteResult> deleteItem(@PathVariable Long id) {
        return Result.ok(systemDictService.deleteItem(id));
    }

    /**
     * 业务读取：按字典标识取启用项，供各业务下拉。
     * 方法级 {@code @RequireAuth} 覆盖类级的 ADMIN 要求——登录即可读，无需管理员。
     */
    @GetMapping("/dicts/{dictCode}")
    @RequireAuth
    public Result<List<DictItemItem>> options(@PathVariable String dictCode) {
        return Result.ok(systemDictService.options(dictCode));
    }
}
