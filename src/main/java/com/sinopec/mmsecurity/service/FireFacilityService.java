package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.FireFacilityAlarmItem;
import com.sinopec.mmsecurity.dto.FireFacilityAlarmResult;
import com.sinopec.mmsecurity.dto.FireFacilityFaultItem;
import com.sinopec.mmsecurity.dto.FireFacilityFaultResult;
import com.sinopec.mmsecurity.dto.FireFacilityFaultTimeline;
import com.sinopec.mmsecurity.dto.FireFacilityLedgerItem;
import com.sinopec.mmsecurity.dto.FireFacilityLedgerResult;
import com.sinopec.mmsecurity.dto.FireFacilityMaintenanceRecord;
import com.sinopec.mmsecurity.dto.FireFacilityMonitorParam;
import com.sinopec.mmsecurity.dto.FireFacilityMonitorResult;
import com.sinopec.mmsecurity.dto.FireFacilityMonitorSummary;
import com.sinopec.mmsecurity.dto.FireFacilityWorkOrderItem;
import com.sinopec.mmsecurity.dto.FireFacilityWorkOrderResult;
import com.sinopec.mmsecurity.entity.FacFireFacilityFault;
import com.sinopec.mmsecurity.entity.FacFireFacilityFaultTimeline;
import com.sinopec.mmsecurity.entity.FacFireFacilityLedger;
import com.sinopec.mmsecurity.entity.FacFireFacilityMaintenance;
import com.sinopec.mmsecurity.entity.FacFireFacilityMonitor;
import com.sinopec.mmsecurity.entity.FacFireFacilityOption;
import com.sinopec.mmsecurity.entity.FacFireFacilityParam;
import com.sinopec.mmsecurity.mapper.FacFireFacilityFaultMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityFaultTimelineMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityLedgerMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityMaintenanceMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityMonitorMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityOptionMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityParamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消防设施监测大屏（fm-fire-facility）服务。
 *
 * <p>数据来源为 V20 落地的 fac_fire_facility_* 真实表，取代前端硬编码的
 * fireFacilityMonitoringMock。报警（fireFacilityAlarms）与维修工单（fireFacilityWorkOrders）
 * 在 mock 中由 fireFacilityFaults 派生，此处沿用同一派生规则实时计算，不单独建表。
 */
@Service
@RequiredArgsConstructor
public class FireFacilityService {

    /** 设施类型下拉的「全部」选项，前端原样回传时按不过滤处理。 */
    private static final String TYPE_ALL = "全部类型";

    /** 下拉选项类别：设施类型。 */
    private static final String KIND_FACILITY_TYPE = "FACILITY_TYPE";

    /** 故障类型中的硬件/通信类，派生报警时归类为「故障」，其余为「动作」。 */
    private static final String FAULT_TYPE_HARDWARE = "硬件故障";
    private static final String FAULT_TYPE_COMM = "通信故障";

    private final FacFireFacilityMonitorMapper monitorMapper;
    private final FacFireFacilityParamMapper paramMapper;
    private final FacFireFacilityLedgerMapper ledgerMapper;
    private final FacFireFacilityMaintenanceMapper maintenanceMapper;
    private final FacFireFacilityFaultMapper faultMapper;
    private final FacFireFacilityFaultTimelineMapper timelineMapper;
    private final FacFireFacilityOptionMapper optionMapper;

    /**
     * 分类监控卡片：按设施类型过滤，空值或「全部类型」返回全部 12 类。
     * 监控参数按 monitor_id 归并到各自卡片。
     */
    public FireFacilityMonitorResult monitors(String facilityType) {
        String typeFilter = normalizeType(facilityType);

        LambdaQueryWrapper<FacFireFacilityMonitor> qw = new LambdaQueryWrapper<>();
        if (typeFilter != null) qw.eq(FacFireFacilityMonitor::getFacilityType, typeFilter);
        qw.orderByAsc(FacFireFacilityMonitor::getSortNo);
        List<FacFireFacilityMonitor> rows = monitorMapper.selectList(qw);

        Map<Long, List<FireFacilityMonitorParam>> paramMap = paramMapper.selectList(
                        new LambdaQueryWrapper<FacFireFacilityParam>()
                                .orderByAsc(FacFireFacilityParam::getSortNo))
                .stream().collect(Collectors.groupingBy(FacFireFacilityParam::getMonitorId,
                        LinkedHashMap::new, Collectors.mapping(this::toParam, Collectors.toList())));

        FireFacilityMonitorResult result = new FireFacilityMonitorResult();
        result.setTypeOptions(typeOptions());
        result.setItems(rows.stream().map(row -> {
            FireFacilityMonitorSummary item = new FireFacilityMonitorSummary();
            item.setKey(row.getKeyCode());
            item.setFacilityType(row.getFacilityType());
            item.setTotal(row.getTotalCount());
            item.setOnline(row.getOnlineCount());
            item.setOffline(row.getOfflineCount());
            item.setFault(row.getFaultCount());
            item.setStatus(row.getMonitorStatus());
            item.setLastReportTime(row.getLastReportTime());
            item.setParams(paramMap.getOrDefault(row.getId(), new ArrayList<>()));
            return item;
        }).collect(Collectors.toList()));
        return result;
    }

    /**
     * 设施台账：按设施类型过滤，空值或「全部类型」返回全部台账。
     * 维保记录按 ledger_id 归并到各自台账条目。
     */
    public FireFacilityLedgerResult ledger(String facilityType) {
        String typeFilter = normalizeType(facilityType);

        LambdaQueryWrapper<FacFireFacilityLedger> qw = new LambdaQueryWrapper<>();
        if (typeFilter != null) qw.eq(FacFireFacilityLedger::getFacilityType, typeFilter);
        qw.orderByAsc(FacFireFacilityLedger::getSortNo);
        List<FacFireFacilityLedger> rows = ledgerMapper.selectList(qw);

        Map<Long, List<FireFacilityMaintenanceRecord>> recordMap = maintenanceMapper.selectList(
                        new LambdaQueryWrapper<FacFireFacilityMaintenance>()
                                .orderByAsc(FacFireFacilityMaintenance::getSortNo))
                .stream().collect(Collectors.groupingBy(FacFireFacilityMaintenance::getLedgerId,
                        LinkedHashMap::new,
                        Collectors.mapping(this::toMaintenanceRecord, Collectors.toList())));

        FireFacilityLedgerResult result = new FireFacilityLedgerResult();
        result.setTypeOptions(typeOptions());
        result.setItems(rows.stream().map(row -> {
            FireFacilityLedgerItem item = new FireFacilityLedgerItem();
            item.setFacilityCode(row.getFacilityCode());
            item.setFacilityName(row.getFacilityName());
            item.setFacilityType(row.getFacilityType());
            item.setLocation(row.getLocationName());
            item.setDevice(row.getDeviceName());
            item.setMaintainerName(row.getMaintainerName());
            item.setMaintainerPhone(row.getMaintainerPhone());
            item.setEnabled(row.getEnabledFlag());
            item.setMaintenanceRecords(recordMap.getOrDefault(row.getId(), new ArrayList<>()));
            return item;
        }).collect(Collectors.toList()));
        return result;
    }

    /** 故障工单列表：faultLevel / faultStatus 均为可选过滤，空值表示不过滤；每条含时间线。 */
    public FireFacilityFaultResult faults(String faultLevel, String faultStatus) {
        FireFacilityFaultResult result = new FireFacilityFaultResult();
        result.setItems(loadFaults(faultLevel, faultStatus));
        return result;
    }

    /**
     * 报警列表：由故障工单派生（id=AL-&lt;故障号数字部分&gt;，硬件/通信故障归类「故障」，其余为「动作」）。
     * level / status 为可选过滤，对应故障等级与故障状态。
     */
    public FireFacilityAlarmResult alarms(String level, String status) {
        FireFacilityAlarmResult result = new FireFacilityAlarmResult();
        result.setItems(loadFaults(level, status).stream().map(this::toAlarm).collect(Collectors.toList()));
        return result;
    }

    /**
     * 维修工单列表：仅保留已生成工单号的故障，工单状态由故障状态映射
     * （待确认/已确认/已派单→已派发，维修中→执行中，待验收→待验收，已闭环→已完成）。
     */
    public FireFacilityWorkOrderResult workOrders(String status) {
        List<FireFacilityWorkOrderItem> items = new ArrayList<>();
        long seq = 0;
        for (FireFacilityFaultItem fault : loadFaults(null, status)) {
            if (fault.getWorkOrderNo() == null || fault.getWorkOrderNo().isBlank()) {
                continue;
            }
            seq++;
            items.add(toWorkOrder(fault, seq));
        }
        FireFacilityWorkOrderResult result = new FireFacilityWorkOrderResult();
        result.setItems(items);
        return result;
    }

    /** 设施类型下拉：取自 fac_fire_facility_option（kind=FACILITY_TYPE）。 */
    private List<String> typeOptions() {
        return optionMapper.selectList(new LambdaQueryWrapper<FacFireFacilityOption>()
                        .eq(FacFireFacilityOption::getKind, KIND_FACILITY_TYPE)
                        .orderByAsc(FacFireFacilityOption::getSortNo))
                .stream().map(FacFireFacilityOption::getOptionLabel).collect(Collectors.toList());
    }

    /** 故障工单查询：level/status 为空表示不过滤；时间线按 fault_id 归并后按 sort_no 升序。 */
    private List<FireFacilityFaultItem> loadFaults(String faultLevel, String faultStatus) {
        String levelFilter = blankToNull(faultLevel);
        String statusFilter = blankToNull(faultStatus);

        LambdaQueryWrapper<FacFireFacilityFault> qw = new LambdaQueryWrapper<>();
        if (levelFilter != null) qw.eq(FacFireFacilityFault::getFaultLevel, levelFilter);
        if (statusFilter != null) qw.eq(FacFireFacilityFault::getFaultStatus, statusFilter);
        qw.orderByAsc(FacFireFacilityFault::getSortNo);
        List<FacFireFacilityFault> rows = faultMapper.selectList(qw);

        Map<Long, List<FireFacilityFaultTimeline>> timelineMap = timelineMapper.selectList(
                        new LambdaQueryWrapper<FacFireFacilityFaultTimeline>()
                                .orderByAsc(FacFireFacilityFaultTimeline::getSortNo))
                .stream().collect(Collectors.groupingBy(FacFireFacilityFaultTimeline::getFaultId,
                        LinkedHashMap::new, Collectors.mapping(this::toTimeline, Collectors.toList())));

        List<FireFacilityFaultItem> items = new ArrayList<>();
        for (FacFireFacilityFault row : rows) {
            FireFacilityFaultItem item = toFault(row);
            item.setTimeline(timelineMap.getOrDefault(row.getId(), new ArrayList<>()));
            items.add(item);
        }
        return items;
    }

    private static String normalizeType(String facilityType) {
        String type = blankToNull(facilityType);
        return TYPE_ALL.equals(type) ? null : type;
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    private static String workOrderStatus(String faultStatus) {
        if (faultStatus == null) {
            return "已派发";
        }
        switch (faultStatus) {
            case "维修中":
                return "执行中";
            case "待验收":
                return "待验收";
            case "已闭环":
                return "已完成";
            default:
                return "已派发";
        }
    }

    private FireFacilityMonitorParam toParam(FacFireFacilityParam e) {
        FireFacilityMonitorParam d = new FireFacilityMonitorParam();
        d.setLabel(e.getLabel());
        d.setValue(e.getValueText());
        d.setTone(e.getTone());
        return d;
    }

    private FireFacilityMaintenanceRecord toMaintenanceRecord(FacFireFacilityMaintenance e) {
        FireFacilityMaintenanceRecord d = new FireFacilityMaintenanceRecord();
        d.setDate(e.getRecordDate());
        d.setContent(e.getContentText());
        d.setReportFile(e.getReportFile());
        return d;
    }

    private FireFacilityFaultTimeline toTimeline(FacFireFacilityFaultTimeline e) {
        FireFacilityFaultTimeline d = new FireFacilityFaultTimeline();
        d.setTime(e.getEventTime());
        d.setOperator(e.getOperatorName());
        d.setAction(e.getActionName());
        d.setDetail(e.getDetailText());
        return d;
    }

    private FireFacilityFaultItem toFault(FacFireFacilityFault e) {
        FireFacilityFaultItem d = new FireFacilityFaultItem();
        d.setId(e.getId());
        d.setFaultCode(e.getFaultCode());
        d.setFacilityCode(e.getFacilityCode());
        d.setFacilityName(e.getFacilityName());
        d.setFacilityType(e.getFacilityType());
        d.setFaultType(e.getFaultType());
        d.setFaultLevel(e.getFaultLevel());
        d.setDiscoverTime(e.getDiscoverTime());
        d.setDiscoverMethod(e.getDiscoverMethod());
        d.setPhenomenon(e.getPhenomenon());
        d.setCause(e.getCauseText());
        d.setStatus(e.getFaultStatus());
        d.setWorkOrderNo(e.getWorkOrderNo());
        d.setRepairPerson(e.getRepairPerson());
        d.setEstimatedFinish(e.getEstimatedFinish());
        d.setActualFinish(e.getActualFinish());
        d.setRepairMeasures(e.getRepairMeasures());
        d.setAcceptancePerson(e.getAcceptancePerson());
        d.setAcceptanceResult(e.getAcceptanceResult());
        return d;
    }

    private FireFacilityAlarmItem toAlarm(FireFacilityFaultItem fault) {
        String faultType = fault.getFaultType();
        boolean deviceFault = FAULT_TYPE_HARDWARE.equals(faultType) || FAULT_TYPE_COMM.equals(faultType);

        FireFacilityAlarmItem d = new FireFacilityAlarmItem();
        d.setId("AL-" + fault.getFaultCode().replace("FLT-", "").replace("-", ""));
        d.setSource(fault.getFacilityType());
        d.setFacilityType(fault.getFacilityType());
        d.setLevel(fault.getFaultLevel());
        d.setCategory(deviceFault ? "故障" : "动作");
        d.setContent(fault.getPhenomenon());
        d.setTime(fault.getDiscoverTime());
        d.setStatus(fault.getStatus());
        d.setFaultCode(fault.getFaultCode());
        return d;
    }

    private FireFacilityWorkOrderItem toWorkOrder(FireFacilityFaultItem fault, long seq) {
        FireFacilityWorkOrderItem d = new FireFacilityWorkOrderItem();
        d.setId(seq);
        d.setWorkOrderNo(fault.getWorkOrderNo());
        d.setFaultCode(fault.getFaultCode());
        d.setFacilityCode(fault.getFacilityCode());
        d.setFacilityName(fault.getFacilityName());
        d.setFacilityType(fault.getFacilityType());
        d.setFaultLevel(fault.getFaultLevel());
        d.setDescription(fault.getPhenomenon());
        d.setStatus(workOrderStatus(fault.getStatus()));
        d.setRepairPerson(fault.getRepairPerson() == null ? "" : fault.getRepairPerson());
        d.setEstimatedFinish(fault.getEstimatedFinish() == null ? "" : fault.getEstimatedFinish());
        d.setActualFinish(fault.getActualFinish());
        d.setDispatchTime(fault.getTimeline().stream()
                .filter(t -> t.getAction() != null && t.getAction().contains("派发"))
                .map(FireFacilityFaultTimeline::getTime)
                .findFirst().orElse(fault.getDiscoverTime()));
        d.setTimeline(fault.getTimeline());
        return d;
    }
}
