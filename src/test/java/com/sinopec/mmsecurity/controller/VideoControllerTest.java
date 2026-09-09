package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.VideoCameraItem;
import com.sinopec.mmsecurity.dto.VideoCameraPage;
import com.sinopec.mmsecurity.dto.VideoCategoryItem;
import com.sinopec.mmsecurity.dto.VideoGroupNode;
import com.sinopec.mmsecurity.dto.VideoNavigation;
import com.sinopec.mmsecurity.service.VideoService;
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

/** 视频控制/视频墙大屏接口校验（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class VideoControllerTest {

    @Mock
    private VideoService service;

    @InjectMocks
    private VideoController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void navigation_returnsCategoriesAndTree() throws Exception {
        VideoNavigation nav = new VideoNavigation();
        VideoCategoryItem category = new VideoCategoryItem();
        category.setId("refining");
        category.setLabel("炼油区");
        category.setIconType(0);
        nav.setCategories(List.of(category));
        VideoGroupNode root = new VideoGroupNode();
        root.setId("drill");
        root.setLabel("应急演练");
        VideoGroupNode child = new VideoGroupNode();
        child.setId("drill-1");
        child.setLabel("演练1");
        root.setChildren(List.of(child));
        nav.setTree(List.of(root));
        when(service.navigation()).thenReturn(nav);

        mvc().perform(get("/api/v1/video/navigation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.categories[0].id").value("refining"))
                .andExpect(jsonPath("$.data.tree[0].children[0].label").value("演练1"));
    }

    @Test
    void cameras_returnsPagedCells() throws Exception {
        VideoCameraPage page = new VideoCameraPage();
        page.setTotal(27L);
        page.setPage(1);
        page.setSize(9);
        page.setPages(3);
        VideoCameraItem camera = new VideoCameraItem();
        camera.setId(2L);
        camera.setName("炼油区-2");
        camera.setCameraType("球机");
        camera.setStatus("loading");
        camera.setHd(true);
        camera.setThumbIndex(1);
        page.setList(List.of(camera));
        when(service.cameras(1, 9)).thenReturn(page);

        mvc().perform(get("/api/v1/video/cameras").param("page", "1").param("size", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(27))
                .andExpect(jsonPath("$.data.pages").value(3))
                .andExpect(jsonPath("$.data.list[0].status").value("loading"));
    }
}
