package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.TaskItem;
import com.sinopec.mmsecurity.dto.TaskList;
import com.sinopec.mmsecurity.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 处置任务接口校验（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static TaskItem sample() {
        TaskItem item = new TaskItem();
        item.setId(1L);
        item.setTaskCode("TASK-001");
        item.setTitle("储运部液化烃储罐现场处置");
        item.setLevel("紧急");
        item.setSource("后台派发");
        item.setArea("储运部 T-301");
        item.setDeadline("2026-08-18 11:30");
        item.setStatus("待接收");
        item.setDescription("前往 T-301 罐区核实泄漏点，反馈现场情况。");
        return item;
    }

    @Test
    void list_returnsItemsAndTotal() throws Exception {
        TaskList list = new TaskList();
        list.setItems(List.of(sample()));
        list.setTotal(1);
        when(taskService.list()).thenReturn(list);

        mvc().perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].taskCode").value("TASK-001"))
                .andExpect(jsonPath("$.data.items[0].level").value("紧急"))
                .andExpect(jsonPath("$.data.items[0].status").value("待接收"));
    }

    @Test
    void detail_returnsItem() throws Exception {
        when(taskService.detail(1L)).thenReturn(sample());

        mvc().perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("储运部液化烃储罐现场处置"))
                .andExpect(jsonPath("$.data.area").value("储运部 T-301"))
                .andExpect(jsonPath("$.data.description").value("前往 T-301 罐区核实泄漏点，反馈现场情况。"));
    }
}
