package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.MsdsDetail;
import com.sinopec.mmsecurity.dto.MsdsList;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.MsdsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 化学品 MSDS 域（移动端化学品知识 / MSDS 详情）。
 *
 * <p>契约：docs/api/msds.openapi.json。纯只读；详情按 CAS 号查询（与移动端 /msds/:cas 路由一致）。
 */
@RestController
@RequestMapping("/api/v1/msds")
@RequireAuth
@RequiredArgsConstructor
public class MsdsController {

    private final MsdsService msdsService;

    /** 化学品 MSDS 列表。 */
    @GetMapping
    public Result<MsdsList> list() {
        return Result.ok(msdsService.list());
    }

    /** 化学品 MSDS 详情（按 CAS 号）。 */
    @GetMapping("/{cas}")
    public Result<MsdsDetail> detail(@PathVariable String cas) {
        return Result.ok(msdsService.detail(cas));
    }
}
