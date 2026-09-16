package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.TaskItem;
import com.sinopec.mmsecurity.dto.TaskList;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 处置任务域（移动端任务中心）。
 *
 * <p>契约：docs/api/tasks.openapi.json。纯只读。<b>语义边界</b>：仅任务台账查询，
 * 不含任何物理下发动作（下行红线见 HardControlPaths）。
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequireAuth
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /** 处置任务列表。 */
    @GetMapping
    public Result<TaskList> list() {
        return Result.ok(taskService.list());
    }

    /** 处置任务详情。 */
    @GetMapping("/{id}")
    public Result<TaskItem> detail(@PathVariable Long id) {
        return Result.ok(taskService.detail(id));
    }
}
