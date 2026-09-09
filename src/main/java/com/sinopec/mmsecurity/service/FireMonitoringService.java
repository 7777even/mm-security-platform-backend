package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.FireEquipmentStatus;
import com.sinopec.mmsecurity.dto.FirePatrolCheckItem;
import com.sinopec.mmsecurity.dto.FirePatrolRecord;
import com.sinopec.mmsecurity.dto.RescueForceStat;
import com.sinopec.mmsecurity.dto.SpecialOperationStat;
import com.sinopec.mmsecurity.entity.FacFireEquipmentStatus;
import com.sinopec.mmsecurity.entity.FacFirePatrol;
import com.sinopec.mmsecurity.entity.FacFirePatrolItemDef;
import com.sinopec.mmsecurity.entity.FacFirePatrolItemResult;
import com.sinopec.mmsecurity.entity.FacRescueForceStat;
import com.sinopec.mmsecurity.entity.FacSpecialOperationStat;
import com.sinopec.mmsecurity.mapper.FacFireEquipmentStatusMapper;
import com.sinopec.mmsecurity.mapper.FacFirePatrolItemDefMapper;
import com.sinopec.mmsecurity.mapper.FacFirePatrolItemResultMapper;
import com.sinopec.mmsecurity.mapper.FacFirePatrolMapper;
import com.sinopec.mmsecurity.mapper.FacRescueForceStatMapper;
import com.sinopec.mmsecurity.mapper.FacSpecialOperationStatMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

    private final FacRescueForceStatMapper rescueForceStatMapper;
    private final FacSpecialOperationStatMapper specialOperationStatMapper;
    private final FacFireEquipmentStatusMapper fireEquipmentStatusMapper;
    private final FacFirePatrolMapper firePatrolMapper;
    private final FacFirePatrolItemDefMapper patrolItemDefMapper;
    private final FacFirePatrolItemResultMapper patrolItemResultMapper;

    /** 消防救援力量统计：来自 fac_rescue_force_stat */
    public List<RescueForceStat> rescueForces() {
        List<FacRescueForceStat> rows = rescueForceStatMapper.selectList(
                new LambdaQueryWrapper<FacRescueForceStat>().orderByAsc(FacRescueForceStat::getSortNo));
        List<RescueForceStat> out = new ArrayList<>();
        for (FacRescueForceStat r : rows) {
            RescueForceStat s = new RescueForceStat();
            s.setLabel(r.getLabel());
            s.setValue(r.getStatCount());
            s.setUnit(r.getUnit());
            s.setIconType(r.getIconType());
            out.add(s);
        }
        return out;
    }

    /** 特殊作业统计：来自 fac_special_operation_stat */
    public List<SpecialOperationStat> specialOperations() {
        List<FacSpecialOperationStat> rows = specialOperationStatMapper.selectList(
                new LambdaQueryWrapper<FacSpecialOperationStat>().orderByAsc(FacSpecialOperationStat::getSortNo));
        List<SpecialOperationStat> out = new ArrayList<>();
        for (FacSpecialOperationStat r : rows) {
            SpecialOperationStat s = new SpecialOperationStat();
            s.setId(r.getId());
            s.setLabel(r.getLabel());
            s.setCount(r.getStatCount());
            out.add(s);
        }
        return out;
    }

    /** 消防设施设备状态：来自 fac_fire_equipment_status 单行聚合表；无数据时返回全零而非 null。 */
    public FireEquipmentStatus equipmentStatus() {
        FacFireEquipmentStatus row = fireEquipmentStatusMapper.selectOne(
                new LambdaQueryWrapper<FacFireEquipmentStatus>().last("LIMIT 1"));
        FireEquipmentStatus s = new FireEquipmentStatus();
        if (row == null) {
            s.setTotal(0);
            s.setOffline(0);
            s.setFault(0);
            s.setIntegrityRate(0);
            s.setOnlineRate(0);
            return s;
        }
        s.setTotal(row.getTotalCnt());
        s.setOffline(row.getOfflineCnt());
        s.setFault(row.getFaultCnt());
        s.setIntegrityRate(row.getIntegrityRate());
        s.setOnlineRate(row.getOnlineRate());
        return s;
    }

    /** 防火巡查记录：标准检查项逐条补齐，异常表覆盖处使用实际结果。 */
    public List<FirePatrolRecord> patrols() {
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
