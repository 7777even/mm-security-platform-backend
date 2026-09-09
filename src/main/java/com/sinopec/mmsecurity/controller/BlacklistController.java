package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.BlacklistSummary;
import com.sinopec.mmsecurity.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 安防黑名单只读接口，数据源为 V21 fac_blacklist_entry 真实表。 */
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
}
