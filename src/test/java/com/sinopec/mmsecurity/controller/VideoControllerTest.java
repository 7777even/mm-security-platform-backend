package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.ImportantVideoFeed;
import com.sinopec.mmsecurity.dto.ImportantVideoGroup;
import com.sinopec.mmsecurity.dto.ImportantVideoGroups;
import com.sinopec.mmsecurity.service.VideoService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * VideoController /important-groups 端点（standalone MockMvc，不启动 Spring 上下文）：
 * 响应字段对齐契约 ImportantVideoGroups/Group/Feed。其余端点由 V14/V37 域测试覆盖。
 */
class VideoControllerTest {

    private final VideoService service = mock(VideoService.class);
    private final VideoController controller = new VideoController(service);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void importantGroups_returnsGroupedFeeds() throws Exception {
        ImportantVideoGroups groups = new ImportantVideoGroups();
        ImportantVideoGroup park = new ImportantVideoGroup();
        park.setId("park");
        park.setLabel("园区全景组");
        ImportantVideoFeed feed = new ImportantVideoFeed();
        feed.setId("ar-park-1");
        feed.setLabel("园区北向全景");
        feed.setImageKey("highAr");
        feed.setPosition("50% 30%");
        feed.setOnline(true);
        park.setFeeds(List.of(feed));
        ImportantVideoGroup boundary = new ImportantVideoGroup();
        boundary.setId("boundary");
        boundary.setLabel("厂界出入口组");
        boundary.setFeeds(List.of());
        groups.setHighArGroups(List.of(park));
        groups.setFocusGroups(List.of(boundary));
        when(service.importantGroups()).thenReturn(groups);

        mockMvc.perform(get("/api/v1/video/important-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.highArGroups[0].id").value("park"))
                .andExpect(jsonPath("$.data.highArGroups[0].label").value("园区全景组"))
                .andExpect(jsonPath("$.data.highArGroups[0].feeds[0].id").value("ar-park-1"))
                .andExpect(jsonPath("$.data.highArGroups[0].feeds[0].imageKey").value("highAr"))
                .andExpect(jsonPath("$.data.highArGroups[0].feeds[0].position").value("50% 30%"))
                .andExpect(jsonPath("$.data.highArGroups[0].feeds[0].online").value(true))
                .andExpect(jsonPath("$.data.focusGroups[0].id").value("boundary"));
    }

    @Test
    void importantGroups_returnsEmptyListsWhenNoData() throws Exception {
        ImportantVideoGroups groups = new ImportantVideoGroups();
        groups.setHighArGroups(List.of());
        groups.setFocusGroups(List.of());
        when(service.importantGroups()).thenReturn(groups);

        mockMvc.perform(get("/api/v1/video/important-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.highArGroups").isEmpty())
                .andExpect(jsonPath("$.data.focusGroups").isEmpty());
    }
}
