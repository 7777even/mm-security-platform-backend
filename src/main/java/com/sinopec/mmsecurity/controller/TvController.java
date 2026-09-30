package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.TvInspectionSummary;
import com.sinopec.mmsecurity.dto.TvMapPoint;
import com.sinopec.mmsecurity.dto.TvMonitorDetail;
import com.sinopec.mmsecurity.dto.TvMonitorSummary;
import com.sinopec.mmsecurity.dto.TvOverview;
import com.sinopec.mmsecurity.dto.TvSnapshotAckResult;
import com.sinopec.mmsecurity.dto.TvSnapshotIngestRequest;
import com.sinopec.mmsecurity.dto.TvSnapshotIngestResult;
import com.sinopec.mmsecurity.dto.TvSnapshotPage;
import com.sinopec.mmsecurity.dto.TvMonitorSummary;
import com.sinopec.mmsecurity.dto.TvMonitorUpsertRequest;
import com.sinopec.mmsecurity.dto.TvMaintenanceOrderItem;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.TvService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.util.concurrent.TimeUnit;

/**
 * 工业电视大屏（fm-tv）接口，数据源为 V15/V24/V78 fac_tv_* 真实表。
 *
 * <p>类级 {@code @RequireAuth}：全部接口需登录（对齐 SecurityController，修复此前 TV 接口裸奔）。
 * 录像截图采集端点（submitSnapshot）需细粒度权限码 {@code video:snapshot:create}
 * （V80 已登记并授权 ADMIN 及岗位角色）；确认端点（ackSnapshot）需 {@code video:snapshot:ack}
 * （V79）。两者均经 {@code @RealtimeSync} 广播 tv.snapshot.changed 触发大屏实时刷新。
 */
@RestController
@RequestMapping("/api/v1/tv")
@RequiredArgsConstructor
@RequireAuth
public class TvController {

    private final TvService tvService;

    /** 首屏聚合：概览卡片 + 运行统计 + 维保工单 + 事件分析。 */
    @GetMapping("/overview")
    public Result<TvOverview> overview() {
        return Result.ok(tvService.overview());
    }

    /**
     * 维修工单明细列表（按状态过滤，status 为空返回全部）。
     * 数据来自 V88 新建的 fac_tv_maintenance_order 真实台账，供概览工单卡片下钻真实工单明细
     * （与大屏「重大危险源」列出真实清单同构）。状态取值：PENDING 未接单 / PROCESSING 处理中 /
     * OVERTIME 已超时。
     */
    @GetMapping("/maintenance-orders")
    public Result<List<TvMaintenanceOrderItem>> maintenanceOrders(
            @RequestParam(required = false) String status) {
        return Result.ok(tvService.listMaintenanceOrders(status));
    }

    /** 单个维修工单明细（按工单 id）。不存在返 404。 */
    @GetMapping("/maintenance-orders/{id}")
    public Result<TvMaintenanceOrderItem> maintenanceOrder(@PathVariable Long id) {
        TvMaintenanceOrderItem item = tvService.getMaintenanceOrder(id);
        if (item == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "维修工单不存在");
        }
        return Result.ok(item);
    }

    /** 入厂巡检聚合：车辆列表 + 人员列表。 */
    @GetMapping("/inspections")
    public Result<TvInspectionSummary> inspections() {
        return Result.ok(tvService.inspections());
    }

    /** 工业电视地图撒点（高空AR/重点部位/危险源/厂界四类）。 */
    @GetMapping("/map-points")
    public Result<List<TvMapPoint>> mapPoints() {
        return Result.ok(tvService.tvMapPoints());
    }

    /** 视频监控点位档案（点击地图撒点时调取）。 */
    @GetMapping("/monitors/{code}")
    public Result<TvMonitorDetail> monitor(@PathVariable String code) {
        TvMonitorDetail detail = tvService.tvMonitorByCode(code);
        if (detail == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "监控点位不存在");
        }
        return Result.ok(detail);
    }

    /**
     * 录像截图采集入库（设备/采集端自助上报或后端采集器拉取）。需细粒度权限码
     * {@code video:snapshot:create}（V80 已登记并授权 ADMIN 及岗位角色）。
     * 成功后广播 tv.snapshot.changed，前端实时刷新。
     */
    @PostMapping("/snapshots")
    @RequireAuth(perm = "video:snapshot:create")
    public Result<TvSnapshotIngestResult> submitSnapshot(@RequestBody TvSnapshotIngestRequest req) {
        return Result.ok(tvService.submitSnapshot(req));
    }

    /**
     * 录像截图分页列表（最新在前）。前端订阅 tv.snapshot.changed 实时刷新。
     * 支持按关联告警 alarmId / alarmType 反向过滤（跨域联动：生产告警详情精准取关联抓拍），
     * 以及按监控点位 monitorCode / 防区 zoneCode / 采集时间区间 startTime~endTime 过滤
     * （「设备/防区筛选」二级页使用）。
     */
    @GetMapping("/snapshots")
    public Result<TvSnapshotPage> snapshots(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long alarmId,
            @RequestParam(required = false) String alarmType,
            @RequestParam(required = false) String monitorCode,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.ok(tvService.listSnapshots(page, size, alarmId, alarmType,
                monitorCode, zone, startTime, endTime));
    }

    /**
     * 视频监控点位摘要列表（设备下拉 / 筛选维度）。返回全部点位（含防区），
     * 供「设备/防区筛选」二级页设备维度下拉使用。
     */
    @GetMapping("/monitors")
    public Result<List<TvMonitorSummary>> monitors() {
        return Result.ok(tvService.listMonitors());
    }

    /**
     * 新增监控点位（设备/防区管理 CRUD）。需权限码 tv:monitor:create（V87 已登记并授权 ADMIN 及岗位角色）。
     * 成功后广播 tv.monitor.changed。
     */
    @PostMapping("/monitors")
    @RequireAuth(perm = "tv:monitor:create")
    public Result<TvMonitorSummary> createMonitor(@RequestBody TvMonitorUpsertRequest req) {
        return Result.ok(tvService.createMonitor(req));
    }

    /**
     * 更新监控点位（含防区归属 zoneCode）。需权限码 tv:monitor:update（V87 已登记并授权）。
     * 成功后广播 tv.monitor.changed。
     */
    @PutMapping("/monitors/{code}")
    @RequireAuth(perm = "tv:monitor:update")
    public Result<TvMonitorSummary> updateMonitor(
            @PathVariable String code, @RequestBody TvMonitorUpsertRequest req) {
        return Result.ok(tvService.updateMonitor(code, req));
    }

    /**
     * 删除监控点位（设备/防区管理 CRUD）。需权限码 tv:monitor:delete（V87 已登记并授权）。
     * 成功后广播 tv.monitor.changed。
     */
    @DeleteMapping("/monitors/{code}")
    @RequireAuth(perm = "tv:monitor:delete")
    public Result<Void> deleteMonitor(@PathVariable String code) {
        tvService.deleteMonitor(code);
        return Result.ok(null);
    }

    /**
     * 设备级历史回放：指定监控点位（monitorCode）的录像截图分页列表（最新在前）。
     * 支持按采集时间区间 startTime~endTime 过滤，供「设备/防区筛选」二级页按设备维度回放历史抓拍。
     */
    @GetMapping("/monitors/{code}/snapshots")
    public Result<TvSnapshotPage> monitorSnapshots(
            @PathVariable String code,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.ok(tvService.monitorSnapshots(code, page, size, startTime, endTime));
    }

    /**
     * 录像截图字节（JPEG）。无数据时 404。
     * 鉴权同 /tv/*（需 JWT），前端用带 token 的 http 客户端取 blob 后转 objectURL 渲染。
     */
    @GetMapping("/snapshots/{id}/snapshot")
    public ResponseEntity<Resource> snapshot(@PathVariable Long id) {
        byte[] bytes = tvService.getSnapshotBytes(id);
        if (bytes == null || bytes.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(new ByteArrayResource(bytes));
    }

    /**
     * 确认录像截图（PENDING→ACKED）。需权限码 video:snapshot:ack（V79 已登记并授权 ADMIN 及岗位角色）。
     * 成功后广播 tv.snapshot.changed。
     */
    @PostMapping("/snapshots/{id}/ack")
    @RequireAuth(perm = "video:snapshot:ack")
    public Result<TvSnapshotAckResult> ackSnapshot(@PathVariable Long id) {
        return Result.ok(tvService.ackSnapshot(id));
    }
}
