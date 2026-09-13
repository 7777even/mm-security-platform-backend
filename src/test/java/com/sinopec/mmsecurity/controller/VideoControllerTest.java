package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.VideoCameraPage;
import com.sinopec.mmsecurity.dto.VideoLinkageItem;
import com.sinopec.mmsecurity.dto.VideoLinkageSaveRequest;
import com.sinopec.mmsecurity.dto.VideoNavigation;
import com.sinopec.mmsecurity.dto.ImportantVideoFeed;
import com.sinopec.mmsecurity.dto.ImportantVideoGroup;
import com.sinopec.mmsecurity.dto.ImportantVideoGroups;
import com.sinopec.mmsecurity.dto.VideoWallNavigation;
import com.sinopec.mmsecurity.service.VideoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VideoControllerTest {

    @Mock
    private VideoService videoService;

    @InjectMocks
    private VideoController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new Validator() {
                    @Override
                    public boolean supports(Class<?> clazz) {
                        return true;
                    }

                    @Override
                    public void validate(Object target, Errors errors) {
                        // 禁用 @Valid 校验，专注测端点装配与 service 调用
                    }
                })
                .build();
    }

    @Test
    void navigation_returnsOk() throws Exception {
        when(videoService.navigation()).thenReturn(new VideoNavigation());
        mvc().perform(get("/api/v1/video/navigation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void wallNavigation_returnsOk() throws Exception {
        when(videoService.wallNavigation()).thenReturn(new VideoWallNavigation());
        mvc().perform(get("/api/v1/video/wall-navigation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void cameras_passesPageSize() throws Exception {
        when(videoService.cameras(2, 12)).thenReturn(new VideoCameraPage());
        mvc().perform(get("/api/v1/video/cameras").param("page", "2").param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

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
        when(videoService.importantGroups()).thenReturn(groups);

        mvc().perform(get("/api/v1/video/important-groups"))
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
        when(videoService.importantGroups()).thenReturn(groups);

        mvc().perform(get("/api/v1/video/important-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.highArGroups").isEmpty())
                .andExpect(jsonPath("$.data.focusGroups").isEmpty());
    }

    @Test
    void linkages_returnsOk() throws Exception {
        mvc().perform(get("/api/v1/video/linkages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void linkageOptions_returnsOk() throws Exception {
        mvc().perform(get("/api/v1/video/linkage-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void createLinkage_postsAndReturnsItem() throws Exception {
        VideoLinkageItem item = new VideoLinkageItem();
        item.setCode("CFG1");
        when(videoService.saveLinkage(org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.any())).thenReturn(item);
        mvc().perform(post("/api/v1/video/linkages").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.code").value("CFG1"));
    }

    @Test
    void updateLinkage_putsAndReturnsItem() throws Exception {
        VideoLinkageItem item = new VideoLinkageItem();
        item.setCode("CFG1");
        when(videoService.saveLinkage(org.mockito.ArgumentMatchers.eq("CFG1"), org.mockito.ArgumentMatchers.any())).thenReturn(item);
        mvc().perform(put("/api/v1/video/linkages/CFG1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("CFG1"));
    }

    @Test
    void deleteLinkage_deletesAndReturnsResult() throws Exception {
        DeleteResult d = new DeleteResult();
        d.setOk(true);
        when(videoService.deleteLinkage("CFG1")).thenReturn(d);
        mvc().perform(delete("/api/v1/video/linkages/CFG1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ok").value(true));
    }

    @Test
    void linkageRules_returnsList() throws Exception {
        mvc().perform(get("/api/v1/video/linkages/CFG1/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void cameraSnapshot_withBytes_returnsJpeg() throws Exception {
        when(videoService.getSnapshotBytes(7L)).thenReturn(new byte[]{1, 2, 3});
        mvc().perform(get("/api/v1/video/cameras/7/snapshot"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    void cameraSnapshot_noBytes_returns404() throws Exception {
        when(videoService.getSnapshotBytes(7L)).thenReturn(null);
        mvc().perform(get("/api/v1/video/cameras/7/snapshot"))
                .andExpect(status().isNotFound());
    }
}
