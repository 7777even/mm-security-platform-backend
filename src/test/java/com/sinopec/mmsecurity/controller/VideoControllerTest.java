package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.VideoCameraItem;
import com.sinopec.mmsecurity.dto.VideoCameraPage;
import com.sinopec.mmsecurity.dto.VideoCategoryItem;
import com.sinopec.mmsecurity.dto.VideoGroupNode;
import com.sinopec.mmsecurity.dto.VideoLinkageItem;
import com.sinopec.mmsecurity.dto.VideoLinkageSaveRequest;
import com.sinopec.mmsecurity.dto.VideoNavigation;
import com.sinopec.mmsecurity.service.VideoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    void createLinkage_returnsCreatedItem() throws Exception {
        VideoLinkageItem item = new VideoLinkageItem();
        item.setId("lk-006");
        item.setName("新监控");
        item.setCode("HKJK-9999999");
        item.setCategory("球机");
        item.setLinkageCount(1);
        item.setBusinessObjects("储油罐区");
        when(service.saveLinkage(isNull(), any(VideoLinkageSaveRequest.class))).thenReturn(item);

        mvc().perform(post("/api/v1/video/linkages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"新监控\",\"code\":\"HKJK-9999999\",\"category\":\"球机\","
                                + "\"rules\":[{\"presetPoint\":\"P1\",\"objectCategory\":\"储罐\","
                                + "\"objectName\":\"储油罐区\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("lk-006"))
                .andExpect(jsonPath("$.data.linkageCount").value(1));
    }

    @Test
    void updateLinkage_passesConfigCode() throws Exception {
        VideoLinkageItem item = new VideoLinkageItem();
        item.setId("lk-001");
        item.setLinkageCount(0);
        when(service.saveLinkage(eq("lk-001"), any(VideoLinkageSaveRequest.class))).thenReturn(item);

        mvc().perform(put("/api/v1/video/linkages/lk-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"n\",\"code\":\"c\",\"category\":\"枪机\",\"rules\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("lk-001"));
    }

    @Test
    void deleteLinkage_returnsDeleteResult() throws Exception {
        DeleteResult result = new DeleteResult();
        result.setOk(true);
        when(service.deleteLinkage("lk-001")).thenReturn(result);

        mvc().perform(delete("/api/v1/video/linkages/lk-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.ok").value(true));
    }
}
