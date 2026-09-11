package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 视频墙 - 导航聚合（取代前端 videoWallStore.ts 内代码生成的本地数据）。
 *
 * <p>targetTree=监测目标树（分类→目标）；videoTree=厂区视频目录（分区→摄像头通道）；
 * cameraTargetMap=摄像头通道编码 → 绑定目标编码列表；defaultHighAltitudeCameras=默认高空AR相机。
 * 摄像头通道编码 v-{i}-{j}（i=目标序号，j=通道序号）由服务端按 TARGET 行 cam_count 派生。</p>
 */
@Data
public class VideoWallNavigation {
    private List<VideoWallGroupNode> targetTree;
    private List<VideoWallGroupNode> videoTree;
    private Map<String, List<String>> cameraTargetMap;
    private List<VideoWallCamera> defaultHighAltitudeCameras;
}
