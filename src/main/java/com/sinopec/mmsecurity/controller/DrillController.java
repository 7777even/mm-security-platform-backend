package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.DrillDetail;
import com.sinopec.mmsecurity.dto.DrillList;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.DrillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 应急演练域（移动端演练信息 / 演练详情）。
 *
 * <p>契约：docs/api/drills.openapi.json。纯只读；不含任何演练执行下发动作。
 */
@RestController
@RequestMapping("/api/v1/drills")
@RequireAuth
@RequiredArgsConstructor
public class DrillController {

    private final DrillService drillService;

    /** 演练列表。 */
    @GetMapping
    public Result<DrillList> list() {
        return Result.ok(drillService.list());
    }

    /** 演练详情（含任务子项）。 */
    @GetMapping("/{id}")
    public Result<DrillDetail> detail(@PathVariable Long id) {
        return Result.ok(drillService.detail(id));
    }
}
