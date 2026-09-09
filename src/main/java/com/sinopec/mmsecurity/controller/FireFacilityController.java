package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.FireFacilityAlarmResult;
import com.sinopec.mmsecurity.dto.FireFacilityFaultResult;
import com.sinopec.mmsecurity.dto.FireFacilityLedgerResult;
import com.sinopec.mmsecurity.dto.FireFacilityMonitorResult;
import com.sinopec.mmsecurity.dto.FireFacilityWorkOrderResult;
import com.sinopec.mmsecurity.service.FireFacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 消防设施监测大屏（fm-fire-facility）只读接口，数据源为 V20 fac_fire_facility_* 真实表。 */
@RestController
@RequestMapping("/api/v1/fire-facility")
@RequiredArgsConstructor
public class FireFacilityController {

    private final FireFacilityService fireFacilityService;

    /** 分类监控卡片：设施类型下拉 + 12 类监控卡片（含监控参数）。facilityType 为空或「全部类型」返回全部。 */
    @GetMapping("/monitors")
    public Result<FireFacilityMonitorResult> monitors(
            @RequestParam(value = "facilityType", required = false) String facilityType) {
        return Result.ok(fireFacilityService.monitors(facilityType));
    }

    /** 设施台账：设施类型下拉 + 台账条目（含维护保养记录）。facilityType 为空或「全部类型」返回全部。 */
    @GetMapping("/ledger")
    public Result<FireFacilityLedgerResult> ledger(
            @RequestParam(value = "facilityType", required = false) String facilityType) {
        return Result.ok(fireFacilityService.ledger(facilityType));
    }

    /** 故障工单：faultLevel / faultStatus 可选过滤，每条含故障时间线。 */
    @GetMapping("/faults")
    public Result<FireFacilityFaultResult> faults(
            @RequestParam(value = "faultLevel", required = false) String faultLevel,
            @RequestParam(value = "faultStatus", required = false) String faultStatus) {
        return Result.ok(fireFacilityService.faults(faultLevel, faultStatus));
    }

    /** 报警列表：由故障工单派生，level / status 可选过滤。 */
    @GetMapping("/alarms")
    public Result<FireFacilityAlarmResult> alarms(
            @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "status", required = false) String status) {
        return Result.ok(fireFacilityService.alarms(level, status));
    }

    /** 维修工单：由已派单故障派生，status 可选过滤（已派发/执行中/待验收/已完成）。 */
    @GetMapping("/work-orders")
    public Result<FireFacilityWorkOrderResult> workOrders(
            @RequestParam(value = "status", required = false) String status) {
        return Result.ok(fireFacilityService.workOrders(status));
    }
}
