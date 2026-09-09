package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.VideoCameraPage;
import com.sinopec.mmsecurity.dto.VideoLinkageItem;
import com.sinopec.mmsecurity.dto.VideoLinkageRuleRow;
import com.sinopec.mmsecurity.dto.VideoNavigation;
import com.sinopec.mmsecurity.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 视频控制/视频墙大屏（fm-video-control / fm-video-wall）只读接口，数据源为 V14 fac_video_* 真实表。 */
@RestController
@RequestMapping("/api/v1/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    /** 左侧导航：顶部分类（扁平）+ 分组树。 */
    @GetMapping("/navigation")
    public Result<VideoNavigation> navigation() {
        return Result.ok(videoService.navigation());
    }

    /** 摄像头分页（默认每页 9 宫格）。 */
    @GetMapping("/cameras")
    public Result<VideoCameraPage> cameras(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size) {
        return Result.ok(videoService.cameras(page, size));
    }

    /** 视频联动配置列表。 */
    @GetMapping("/linkages")
    public Result<List<VideoLinkageItem>> linkages() {
        return Result.ok(videoService.linkages());
    }

    /** 指定联动配置的规则行；未预置规则时回退默认行。 */
    @GetMapping("/linkages/{configCode}/rules")
    public Result<List<VideoLinkageRuleRow>> linkageRules(@PathVariable String configCode) {
        return Result.ok(videoService.linkageRules(configCode));
    }
}
