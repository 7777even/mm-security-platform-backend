package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.TaskItem;
import com.sinopec.mmsecurity.dto.TaskList;
import com.sinopec.mmsecurity.entity.FacDispatchTask;
import com.sinopec.mmsecurity.mapper.FacDispatchTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 处置任务域服务（移动端任务中心）。
 *
 * <p>数据来源为 fac_dispatch_task 真实表，取代 apps/mobile/data/mock.ts 的 tasks 静态数据。
 * 纯只读：列表 + 按 id 详情。
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final FacDispatchTaskMapper taskMapper;

    /** 处置任务列表（按 id 升序）。 */
    public TaskList list() {
        List<FacDispatchTask> rows = taskMapper.selectList(
                new LambdaQueryWrapper<FacDispatchTask>().orderByAsc(FacDispatchTask::getId));
        List<TaskItem> items = rows.stream().map(TaskService::toItem).toList();
        TaskList result = new TaskList();
        result.setItems(items);
        result.setTotal(items.size());
        return result;
    }

    /** 处置任务详情；未命中抛 NOT_FOUND。 */
    public TaskItem detail(Long id) {
        FacDispatchTask row = taskMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "处置任务不存在：id=" + id);
        }
        return toItem(row);
    }

    private static TaskItem toItem(FacDispatchTask row) {
        TaskItem item = new TaskItem();
        item.setId(row.getId());
        item.setTaskCode(row.getTaskCode());
        item.setTitle(row.getTitle());
        item.setLevel(row.getLevel());
        item.setSource(row.getSource());
        item.setArea(row.getArea());
        item.setDeadline(row.getDeadline());
        item.setStatus(row.getStatus());
        item.setDescription(row.getDescription());
        return item;
    }
}
