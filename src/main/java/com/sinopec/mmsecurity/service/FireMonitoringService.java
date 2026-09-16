package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.FireEquipmentItem;
import com.sinopec.mmsecurity.dto.FireEquipmentStatus;
import com.sinopec.mmsecurity.dto.FirePatrolCheckItem;
import com.sinopec.mmsecurity.dto.FirePatrolRecord;
import com.sinopec.mmsecurity.dto.RescueForceStat;
import com.sinopec.mmsecurity.dto.SpecialOperationStat;
import com.sinopec.mmsecurity.entity.FacFireFacilityMonitor;
import com.sinopec.mmsecurity.entity.FacFirePatrol;
import com.sinopec.mmsecurity.entity.FacFirePatrolItemDef;
import com.sinopec.mmsecurity.entity.FacFirePatrolItemResult;
import com.sinopec.mmsecurity.entity.FacSpecialOperationStat;
import com.sinopec.mmsecurity.entity.FacSpecialOperationTicket;
import com.sinopec.mmsecurity.mapper.FacBrigadeEquipmentMapper;
import com.sinopec.mmsecurity.mapper.FacBrigadePersonMapper;
import com.sinopec.mmsecurity.mapper.FacBrigadeTeamMapper;
import com.sinopec.mmsecurity.mapper.FacBrigadeVehicleMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityMonitorMapper;
import com.sinopec.mmsecurity.mapper.FacFirePatrolItemDefMapper;
import com.sinopec.mmsecurity.mapper.FacFirePatrolItemResultMapper;
import com.sinopec.mmsecurity.mapper.FacFirePatrolMapper;
import com.sinopec.mmsecurity.mapper.FacRescueForceStatMapper;
import com.sinopec.mmsecurity.mapper.FacSpecialOperationStatMapper;
import com.sinopec.mmsecurity.mapper.FacSpecialOperationTicketMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.time.Duration;
import java.util.stream.Collectors;

/**
 * 消防监控大屏（fm-fire）统计与巡查服务。
 *
 * <p>数据来源全部为 V10 迁移落地的真实表（fac_rescue_force_stat / fac_special_operation_stat /
 * fac_fire_equipment_status / fac_fire_patrol / fac_fire_patrol_item_def / fac_fire_patrol_item_result），
 * 取代此前前端硬编码的 rescueStats / specialOperations / equipmentStatus / firePatrolRecords。
 *
 * <p>防火巡查检查项采用「标准表 + 异常表」的存储策略：标准表 fac_fire_patrol_item_def 保存 15 项
 * 检查项定义，异常表 fac_fire_patrol_item_result 仅保存非「正常」的结果（异常 / 不适用），
 * 服务端在读取时按标准表逐项补齐为「正常」，从而避免每条巡查冗余存储 14 行。
 */
@Service
@RequiredArgsConstructor
public class FireMonitoringService {

    /** 检查项默认结果：标准表中未被异常表覆盖的项一律为「正常」 */
    private static final String RESULT_NORMAL = "正常";

    /** 特殊作业类别字典（仅提供类别与排序，数量改由明细票表 fac_special_operation_ticket 实时计数）。 */
    private final FacSpecialOperationStatMapper specialOperationStatMapper;
    private final FacSpecialOperationTicketMapper specialOperationTicketMapper;
    /** 消防设施监测（与 GET /fire-facility/monitors 同源）——消防设备分类与状态的唯一数据源。 */
    private final FacFireFacilityMonitorMapper fireFacilityMonitorMapper;
    /** 消防救援力量（与 GET /rescue-resources/brigades 同源的队伍体系）：队伍 + 各队人员/装备/车辆明细。 */
    private final FacBrigadeTeamMapper brigadeTeamMapper;
    private final FacBrigadePersonMapper brigadePersonMapper;
    private final FacBrigadeEquipmentMapper brigadeEquipmentMapper;
    private final FacBrigadeVehicleMapper brigadeVehicleMapper;
    private final FacFirePatrolMapper firePatrolMapper;
    private final FacFirePatrolItemDefMapper patrolItemDefMapper;
    private final FacFirePatrolItemResultMapper patrolItemResultMapper;

    /**
     * 防火巡查记录短 TTL 缓存：patrols() 读 fac_fire_patrol_item_def + fac_fire_patrol + 全量异常结果
     * （patrolItemResultMapper.selectList(null) 为全表扫描），大屏高频轮询入口。
     * 只读无写入口，TTL 即最终一致窗口。
     */
    private final Cache<String, List<FirePatrolRecord>> patrolsCache =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)).maximumSize(1).build();

    /**
     * 消防救援力量：真源统一为「队伍体系」fac_brigade_*（与管理端 GET /rescue-resources/brigades 同源）——
     * 队伍数取队伍表条数，人员/装备/车辆取各队明细表条数；取代原先手填的 fac_rescue_force_stat
     * （10 支 / 398 人 / 123 套 / 83 台 与实际队伍编制完全脱节）。
     */
    public List<RescueForceStat> rescueForces() {
        List<RescueForceStat> out = new ArrayList<>();
        out.add(forceStat("消防队伍", brigadeTeamMapper.selectCount(null), "支", "squad"));
        out.add(forceStat("救援人员", brigadePersonMapper.selectCount(null), "人", "person"));
        out.add(forceStat("救援装备", brigadeEquipmentMapper.selectCount(null), "套", "equipment"));
        out.add(forceStat("救援车辆", brigadeVehicleMapper.selectCount(null), "台", "vehicle"));
        return out;
    }

    private static RescueForceStat forceStat(String label, Long count, String unit, String iconType) {
        RescueForceStat s = new RescueForceStat();
        s.setLabel(label);
        s.setValue(count == null ? 0 : count.intValue());
        s.setUnit(unit);
        s.setIconType(iconType);
        return s;
    }

    /**
     * 特殊作业统计：类别与顺序取自 fac_special_operation_stat（降级为「类别字典」），
     * 数量改为按明细票表 fac_special_operation_ticket 的 op_type 实时计数
     * ——与管理端 GET /special-operations 的明细同源，取代原先手填的 stat_count（48 vs 实际票数）。
     * 无票的类别保留 0（前端据此渲染灰色零值态）。
     */
    public List<SpecialOperationStat> specialOperations() {
        List<FacSpecialOperationStat> dict = specialOperationStatMapper.selectList(
                new LambdaQueryWrapper<FacSpecialOperationStat>().orderByAsc(FacSpecialOperationStat::getSortNo));
        Map<String, Long> countByType = specialOperationTicketMapper.selectList(null).stream()
                .filter(t -> t.getOpType() != null)
                .collect(Collectors.groupingBy(FacSpecialOperationTicket::getOpType, Collectors.counting()));
        List<SpecialOperationStat> out = new ArrayList<>();
        for (FacSpecialOperationStat r : dict) {
            SpecialOperationStat s = new SpecialOperationStat();
            s.setId(r.getId());
            s.setLabel(r.getLabel());
            s.setCount(countByType.getOrDefault(r.getLabel(), 0L).intValue());
            out.add(s);
        }
        return out;
    }

    /**
     * 消防设备分类清单：真源统一为「消防设施监测」fac_fire_facility_monitor
     * （与管理端 GET /fire-facility/monitors 完全同源、同一套设施分类），按类型汇总设备台数。
     * 取代原先手填的 fac_fire_equipment_category（分类名相同但数量完全脱节：7980 vs 983）。
     */
    public List<FireEquipmentItem> equipment() {
        List<FacFireFacilityMonitor> rows = fireFacilityMonitorMapper.selectList(
                new LambdaQueryWrapper<FacFireFacilityMonitor>().orderByAsc(FacFireFacilityMonitor::getSortNo));
        List<FireEquipmentItem> out = new ArrayList<>();
        for (FacFireFacilityMonitor m : rows) {
            FireEquipmentItem item = new FireEquipmentItem();
            item.setId(m.getId());
            item.setName(m.getFacilityType());
            item.setCount(m.getTotalCount() == null ? 0 : m.getTotalCount());
            out.add(item);
        }
        return out;
    }

    /**
     * 消防设施设备状态：由 fac_fire_facility_monitor 逐类型汇总（total/offline/fault 求和，
     * 在线率/完好率实时计算），与 GET /fire-facility/monitors 同源；
     * 取代原先手填的 fac_fire_equipment_status 单行表（1233 与监测表 983 脱节）。无数据返回全零而非 null。
     */
    public FireEquipmentStatus equipmentStatus() {
        List<FacFireFacilityMonitor> rows = fireFacilityMonitorMapper.selectList(null);
        int total = 0;
        int offline = 0;
        int fault = 0;
        for (FacFireFacilityMonitor m : rows) {
            total += m.getTotalCount() == null ? 0 : m.getTotalCount();
            offline += m.getOfflineCount() == null ? 0 : m.getOfflineCount();
            fault += m.getFaultCount() == null ? 0 : m.getFaultCount();
        }
        FireEquipmentStatus s = new FireEquipmentStatus();
        s.setTotal(total);
        s.setOffline(offline);
        s.setFault(fault);
        s.setOnlineRate(total <= 0 ? 0 : Math.round((total - offline) * 100f / total));
        s.setIntegrityRate(total <= 0 ? 0 : Math.round((total - fault) * 100f / total));
        return s;
    }

    /** 防火巡查记录：标准检查项逐条补齐，异常表覆盖处使用实际结果（带短 TTL 缓存）。 */
    public List<FirePatrolRecord> patrols() {
        return patrolsCache.get("PATROLS", k -> computePatrols());
    }

    private List<FirePatrolRecord> computePatrols() {
        List<FacFirePatrolItemDef> defs = patrolItemDefMapper.selectList(
                new LambdaQueryWrapper<FacFirePatrolItemDef>().orderByAsc(FacFirePatrolItemDef::getSortNo));
        List<FacFirePatrol> rows = firePatrolMapper.selectList(
                new LambdaQueryWrapper<FacFirePatrol>().orderByDesc(FacFirePatrol::getPatrolDate));

        // 一次性加载全部异常结果并按 patrolId 分组，避免逐条查询（N+1）
        Map<Long, Map<String, FacFirePatrolItemResult>> abnormalByPatrol =
                patrolItemResultMapper.selectList(null).stream()
                        .collect(Collectors.groupingBy(
                                FacFirePatrolItemResult::getPatrolId,
                                Collectors.toMap(FacFirePatrolItemResult::getItemCode, Function.identity(),
                                        (a, b) -> a)));

        List<FirePatrolRecord> out = new ArrayList<>();
        for (FacFirePatrol p : rows) {
            FirePatrolRecord rec = new FirePatrolRecord();
            rec.setId(p.getId());
            rec.setPatrolDate(p.getPatrolDate());
            rec.setShift(p.getShiftName());
            rec.setDutyPerson(p.getDutyPerson());
            rec.setPatrolCount(p.getPatrolCount());
            rec.setLocations(splitLocations(p.getLocations()));
            rec.setCompleted(Boolean.TRUE.equals(p.getCompleted()));
            rec.setWorkOrderNo(p.getWorkOrderNo());

            Map<String, FacFirePatrolItemResult> abnormal =
                    abnormalByPatrol.getOrDefault(p.getId(), Collections.emptyMap());
            List<FirePatrolCheckItem> items = new ArrayList<>();
            for (FacFirePatrolItemDef def : defs) {
                FirePatrolCheckItem item = new FirePatrolCheckItem();
                item.setItemCode(def.getItemCode());
                item.setCategory(def.getCategory());
                item.setContent(def.getContent());
                FacFirePatrolItemResult hit = abnormal.get(def.getItemCode());
                if (hit != null) {
                    item.setResult(hit.getCheckResult());
                    item.setAbnormalDesc(hit.getAbnormalDesc());
                    item.setPhotoFile(hit.getPhotoFile());
                } else {
                    item.setResult(RESULT_NORMAL);
                }
                items.add(item);
            }
            rec.setCheckItems(items);
            out.add(rec);
        }
        return out;
    }

    /** 测试隔离用：清空巡查缓存，避免跨用例污染。 */
    void clearCaches() {
        patrolsCache.invalidateAll();
    }

    /** locations 以逗号分隔存储，输出为列表；空串返回空列表而非空串元素。 */
    private static List<String> splitLocations(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
