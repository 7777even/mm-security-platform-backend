package com.sinopec.mmsecurity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.ZoneItem;
import com.sinopec.mmsecurity.entity.SysZone;
import com.sinopec.mmsecurity.mapper.SysZoneMapper;
import com.sinopec.mmsecurity.security.RequireAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 防区下拉接口（data_scope 行级 ABAC 维度主数据）。
 *
 * <p>仅登录可读（{@code @RequireAuth} 无 role/perm 参数），供系统管理用户表单的「可访问防区」
 * 多选，以及未来接入 ABAC 的业务域做防区维度源。</p>
 */
@RestController
@RequestMapping("/api/v1/system/zones")
@RequireAuth
@RequiredArgsConstructor
public class SystemZoneController {

    private final SysZoneMapper zoneMapper;

    /** 防区列表（启用且未删除，按 sort_order 升序）。 */
    @GetMapping
    public Result<List<ZoneItem>> list() {
        List<SysZone> rows = zoneMapper.selectList(new LambdaQueryWrapper<SysZone>()
                .eq(SysZone::getStatus, 1)
                .eq(SysZone::getDeleted, 0)
                .orderByAsc(SysZone::getSortOrder));
        return Result.ok(rows.stream().map(this::toItem).collect(Collectors.toList()));
    }

    private ZoneItem toItem(SysZone z) {
        ZoneItem item = new ZoneItem();
        item.setId(z.getId());
        item.setZoneCode(z.getZoneCode());
        item.setZoneName(z.getZoneName());
        item.setSortOrder(z.getSortOrder());
        item.setStatus(z.getStatus());
        return item;
    }
}
