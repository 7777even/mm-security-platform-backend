package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.PersonnelMarker;
import com.sinopec.mmsecurity.dto.ProductionAlarmItem;
import com.sinopec.mmsecurity.dto.ProductionAreaDetail;
import com.sinopec.mmsecurity.dto.ProductionDevicePage;
import com.sinopec.mmsecurity.dto.ProductionOverview;
import com.sinopec.mmsecurity.dto.RiskWarningItem;
import com.sinopec.mmsecurity.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 生产应急监测大屏接口（fm-production / fm-production-area）。
 * 全部读取 V13 落地的 fac_production_* 真实表，取代前端硬编码的 productionMock 系列。
 */
@RestController
@RequestMapping("/api/v1/production")
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService service;

    /** 首屏总览：设施卡片 + 设备分类卡片 + 统计概览条 + 风险汇总。 */
    @GetMapping("/overview")
    public Result<ProductionOverview> overview() {
        return Result.ok(service.overview());
    }

    /** 生产报警列表；facilityId 为空返回全部，传入时按设施过滤。 */
    @GetMapping("/alarms")
    public Result<List<ProductionAlarmItem>> alarms(
            @RequestParam(value = "facilityId", required = false) Long facilityId) {
        return Result.ok(service.alarms(facilityId));
    }

    /** 风险预警列表（红/橙/黄三级）。 */
    @GetMapping("/risk-warnings")
    public Result<List<RiskWarningItem>> riskWarnings() {
        return Result.ok(service.riskWarnings());
    }

    /** 人员定位标记（版面百分比坐标 + WGS84 经纬度）。 */
    @GetMapping("/personnel")
    public Result<List<PersonnelMarker>> personnel() {
        return Result.ok(service.personnel());
    }

    /** 装置区二级页聚合详情；设施未命中返回 NOT_FOUND 业务码。 */
    @GetMapping("/areas/{facilityId}")
    public Result<ProductionAreaDetail> areaDetail(@PathVariable("facilityId") Long facilityId) {
        ProductionAreaDetail detail = service.areaDetail(facilityId);
        if (detail == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "装置区不存在：" + facilityId);
        }
        return Result.ok(detail);
    }

    /** 设备清单分页；category / status 可选过滤，「全部状态」表示不过滤。 */
    @GetMapping("/devices")
    public Result<ProductionDevicePage> devices(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        return Result.ok(service.devices(category, status, page, size));
    }
}
