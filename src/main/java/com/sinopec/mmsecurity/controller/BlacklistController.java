package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.BlacklistSummary;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 安防黑名单接口：聚合只读，车辆/人员条目支持删除，数据源为 V21 fac_blacklist_entry 真实表。 */
@RestController
@RequestMapping("/api/v1/security")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    /** 黑名单聚合：车辆黑名单 + 人员黑名单。 */
    @GetMapping("/blacklist")
    public Result<BlacklistSummary> blacklist() {
        return Result.ok(blacklistService.blacklist());
    }

    /** 从车辆黑名单移除记录。 */
    @DeleteMapping("/blacklist/vehicles/{id}")
    @RequireAuth(role = "ADMIN")
    public Result<DeleteResult> removeVehicle(@PathVariable Long id) {
        return Result.ok(blacklistService.removeVehicle(id));
    }

    /** 从人员黑名单移除记录。 */
    @DeleteMapping("/blacklist/persons/{id}")
    @RequireAuth(role = "ADMIN")
    public Result<DeleteResult> removePerson(@PathVariable Long id) {
        return Result.ok(blacklistService.removePerson(id));
    }
}
