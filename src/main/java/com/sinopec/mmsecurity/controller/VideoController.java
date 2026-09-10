package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.VideoCameraPage;
import com.sinopec.mmsecurity.dto.VideoLinkageItem;
import com.sinopec.mmsecurity.dto.VideoLinkageRuleRow;
import com.sinopec.mmsecurity.dto.VideoLinkageSaveRequest;
import com.sinopec.mmsecurity.dto.VideoNavigation;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    /** 新建视频联动配置（configCode 由服务端生成）。 */
    @PostMapping("/linkages")
    @RequireAuth(role = "ADMIN")
    public Result<VideoLinkageItem> createLinkage(@Valid @RequestBody VideoLinkageSaveRequest payload) {
        return Result.ok(videoService.saveLinkage(null, payload));
    }

    /** 更新视频联动配置及其规则行；未命中 configCode 时 data 为 null。 */
    @PutMapping("/linkages/{configCode}")
    @RequireAuth(role = "ADMIN")
    public Result<VideoLinkageItem> updateLinkage(
            @PathVariable String configCode, @Valid @RequestBody VideoLinkageSaveRequest payload) {
        return Result.ok(videoService.saveLinkage(configCode, payload));
    }

    /** 删除视频联动配置及其规则行；未命中时 ok=false。 */
    @DeleteMapping("/linkages/{configCode}")
    @RequireAuth(role = "ADMIN")
    public Result<DeleteResult> deleteLinkage(@PathVariable String configCode) {
        return Result.ok(videoService.deleteLinkage(configCode));
    }

    /** 指定联动配置的规则行；未预置规则时回退默认行。 */
    @GetMapping("/linkages/{configCode}/rules")
    public Result<List<VideoLinkageRuleRow>> linkageRules(@PathVariable String configCode) {
        return Result.ok(videoService.linkageRules(configCode));
    }

    /**
     * 摄像头静态截图（演示）：返回 snapshot_bytes 列中的 JPEG 字节。
     * 当前为 dev seeder 生成的占位图；后续接真流时替换为媒体网关转发的流地址/截图。
     * 鉴权同 /video/*（需 JWT），直接走字节端点（前端用带 token 的 http 客户端取 blob）。
     */
    @GetMapping("/cameras/{id}/snapshot")
    public ResponseEntity<Resource> cameraSnapshot(@PathVariable Long id) {
        byte[] bytes = videoService.getSnapshotBytes(id);
        if (bytes == null || bytes.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(new ByteArrayResource(bytes));
    }
}
