package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.DrillDetail;
import com.sinopec.mmsecurity.dto.DrillItem;
import com.sinopec.mmsecurity.dto.DrillList;
import com.sinopec.mmsecurity.dto.DrillTaskItem;
import com.sinopec.mmsecurity.entity.FacDrill;
import com.sinopec.mmsecurity.entity.FacDrillTask;
import com.sinopec.mmsecurity.mapper.FacDrillMapper;
import com.sinopec.mmsecurity.mapper.FacDrillTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 应急演练域服务（移动端演练信息 / 演练详情）。
 *
 * <p>数据来源为 fac_drill + fac_drill_task 真实表，取代 apps/mobile/data/mock.ts 的 drills 静态数据。
 * 纯只读：列表（含任务数）+ 按 id 详情（含任务子项）。
 */
@Service
@RequiredArgsConstructor
public class DrillService {

    private final FacDrillMapper drillMapper;
    private final FacDrillTaskMapper drillTaskMapper;

    /** 演练列表（按 id 升序，含任务数）。 */
    public DrillList list() {
        List<FacDrill> drills = drillMapper.selectList(
                new LambdaQueryWrapper<FacDrill>().orderByAsc(FacDrill::getId));
        Map<Long, Long> countByDrill = drillTaskMapper.selectList(new LambdaQueryWrapper<>())
                .stream()
                .collect(Collectors.groupingBy(FacDrillTask::getDrillId, Collectors.counting()));
        List<DrillItem> items = drills.stream()
                .map(d -> toItem(d, countByDrill.getOrDefault(d.getId(), 0L).intValue()))
                .toList();
        DrillList result = new DrillList();
        result.setItems(items);
        result.setTotal(items.size());
        return result;
    }

    /** 演练详情（含任务子项）；未命中抛 NOT_FOUND。 */
    public DrillDetail detail(Long id) {
        FacDrill drill = drillMapper.selectById(id);
        if (drill == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "演练不存在：id=" + id);
        }
        List<DrillTaskItem> tasks = drillTaskMapper.selectList(
                        new LambdaQueryWrapper<FacDrillTask>()
                                .eq(FacDrillTask::getDrillId, id)
                                .orderByAsc(FacDrillTask::getId))
                .stream()
                .map(t -> {
                    DrillTaskItem it = new DrillTaskItem();
                    it.setName(t.getName());
                    it.setStatus(t.getStatus());
                    return it;
                })
                .toList();
        DrillDetail detail = new DrillDetail();
        detail.setId(drill.getId());
        detail.setDrillCode(drill.getDrillCode());
        detail.setName(drill.getName());
        detail.setDrillType(drill.getDrillType());
        detail.setForm(drill.getForm());
        detail.setTimeRange(drill.getTimeRange());
        detail.setPlace(drill.getPlace());
        detail.setStatus(drill.getStatus());
        detail.setDepartments(drill.getDepartments());
        detail.setTasks(tasks);
        return detail;
    }

    private static DrillItem toItem(FacDrill d, int taskCount) {
        DrillItem item = new DrillItem();
        item.setId(d.getId());
        item.setDrillCode(d.getDrillCode());
        item.setName(d.getName());
        item.setDrillType(d.getDrillType());
        item.setForm(d.getForm());
        item.setTimeRange(d.getTimeRange());
        item.setPlace(d.getPlace());
        item.setStatus(d.getStatus());
        item.setDepartments(d.getDepartments());
        item.setTaskCount(taskCount);
        return item;
    }
}
