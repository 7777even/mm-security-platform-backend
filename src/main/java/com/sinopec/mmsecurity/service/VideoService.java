package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.VideoCameraItem;
import com.sinopec.mmsecurity.dto.VideoCameraPage;
import com.sinopec.mmsecurity.dto.ImportantVideoFeed;
import com.sinopec.mmsecurity.dto.ImportantVideoGroup;
import com.sinopec.mmsecurity.dto.ImportantVideoGroups;
import com.sinopec.mmsecurity.dto.VideoCategoryItem;
import com.sinopec.mmsecurity.dto.VideoGroupNode;
import com.sinopec.mmsecurity.dto.VideoLinkageItem;
import com.sinopec.mmsecurity.dto.VideoLinkageOptions;
import com.sinopec.mmsecurity.dto.VideoLinkageRuleInput;
import com.sinopec.mmsecurity.dto.VideoLinkageRuleRow;
import com.sinopec.mmsecurity.dto.VideoLinkageSaveRequest;
import com.sinopec.mmsecurity.dto.VideoNavigation;
import com.sinopec.mmsecurity.dto.VideoWallCamera;
import com.sinopec.mmsecurity.dto.VideoWallGroupNode;
import com.sinopec.mmsecurity.dto.VideoWallNavigation;
import com.sinopec.mmsecurity.entity.FacVideoCamera;
import com.sinopec.mmsecurity.entity.FacVideoGroup;
import com.sinopec.mmsecurity.entity.FacVideoImportantFeed;
import com.sinopec.mmsecurity.entity.FacVideoImportantGroup;
import com.sinopec.mmsecurity.entity.FacVideoLinkage;
import com.sinopec.mmsecurity.entity.FacVideoLinkageOption;
import com.sinopec.mmsecurity.entity.FacVideoLinkageRule;
import com.sinopec.mmsecurity.entity.FacVideoWallNode;
import com.sinopec.mmsecurity.mapper.FacVideoCameraMapper;
import com.sinopec.mmsecurity.mapper.FacVideoGroupMapper;
import com.sinopec.mmsecurity.mapper.FacVideoImportantFeedMapper;
import com.sinopec.mmsecurity.mapper.FacVideoImportantGroupMapper;
import com.sinopec.mmsecurity.mapper.FacVideoLinkageMapper;
import com.sinopec.mmsecurity.mapper.FacVideoLinkageOptionMapper;
import com.sinopec.mmsecurity.mapper.FacVideoLinkageRuleMapper;
import com.sinopec.mmsecurity.mapper.FacVideoWallNodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 视频控制/视频墙大屏（fm-video-control / fm-video-wall）服务。
 *
 * <p>数据来源为 V14 落地的 fac_video_* 真实表，取代前端硬编码的 videoControlMock /
 * videoLinkageMock。网格布局（1x1/2x2/3x3）与画面轮巡为前端交互状态，不在此处理。
 */
@Service
@RequiredArgsConstructor
public class VideoService {

    private static final String KIND_CATEGORY = "CATEGORY";
    private static final String KIND_TREE = "TREE";

    // fac_video_wall_node（V37）节点种类
    private static final String WALL_KIND_CATEGORY = "CATEGORY";
    private static final String WALL_KIND_AREA = "AREA";
    private static final String WALL_KIND_HIGH_AR = "HIGH_AR";
    private static final String WALL_KIND_TARGET = "TARGET";

    private final FacVideoGroupMapper groupMapper;
    private final FacVideoCameraMapper cameraMapper;
    private final FacVideoLinkageMapper linkageMapper;
    private final FacVideoLinkageRuleMapper linkageRuleMapper;
    private final FacVideoLinkageOptionMapper linkageOptionMapper;
    private final FacVideoWallNodeMapper wallNodeMapper;
    private final FacVideoImportantGroupMapper importantGroupMapper;
    private final FacVideoImportantFeedMapper importantFeedMapper;

    /** 左侧导航：顶部分类（扁平）+ 分组树。 */
    public VideoNavigation navigation() {
        VideoNavigation nav = new VideoNavigation();
        nav.setCategories(groupMapper.selectList(new LambdaQueryWrapper<FacVideoGroup>()
                        .eq(FacVideoGroup::getGroupKind, KIND_CATEGORY)
                        .orderByAsc(FacVideoGroup::getSortNo)).stream()
                .map(g -> {
                    VideoCategoryItem item = new VideoCategoryItem();
                    item.setId(g.getNodeCode());
                    item.setLabel(g.getLabel());
                    item.setIconType(g.getIconIndex());
                    return item;
                }).collect(Collectors.toList()));

        List<FacVideoGroup> treeNodes = groupMapper.selectList(new LambdaQueryWrapper<FacVideoGroup>()
                .eq(FacVideoGroup::getGroupKind, KIND_TREE)
                .orderByAsc(FacVideoGroup::getSortNo));
        Map<String, List<FacVideoGroup>> byParent = treeNodes.stream()
                .filter(g -> g.getParentCode() != null)
                .collect(Collectors.groupingBy(FacVideoGroup::getParentCode));
        nav.setTree(treeNodes.stream()
                .filter(g -> g.getParentCode() == null)
                .map(g -> toGroupNode(g, byParent))
                .collect(Collectors.toList()));
        return nav;
    }

    private VideoGroupNode toGroupNode(FacVideoGroup group, Map<String, List<FacVideoGroup>> byParent) {
        VideoGroupNode node = new VideoGroupNode();
        node.setId(group.getNodeCode());
        node.setLabel(group.getLabel());
        List<FacVideoGroup> children = byParent.get(group.getNodeCode());
        if (children != null && !children.isEmpty()) {
            node.setChildren(children.stream()
                    .map(child -> toGroupNode(child, byParent))
                    .collect(Collectors.toList()));
        }
        return node;
    }

    /**
     * 视频墙导航聚合（V37 fac_video_wall_node）：监测目标树 + 厂区视频目录 +
     * 通道→目标映射 + 默认高空AR相机。取代前端 videoWallStore.ts 内代码生成的本地数据。
     *
     * <p>摄像头通道（v-{i}-{j}，i=目标 sort_no，j=1..cam_count）与其到目标的映射由
     * TARGET 行确定性派生（占位通道命名，接真实 MDM 设备后改读设备表）；
     * 目标树/厂区分区/高空AR相机均为表内真实行。</p>
     */
    public VideoWallNavigation wallNavigation() {
        List<FacVideoWallNode> nodes = wallNodeMapper.selectList(new LambdaQueryWrapper<FacVideoWallNode>()
                .orderByAsc(FacVideoWallNode::getSortNo)
                .orderByAsc(FacVideoWallNode::getId));

        List<VideoWallGroupNode> targetTree = nodes.stream()
                .filter(n -> WALL_KIND_CATEGORY.equals(n.getNodeKind()))
                .map(category -> {
                    VideoWallGroupNode node = leafNode(category.getNodeCode(), category.getLabel());
                    node.setChildren(nodes.stream()
                            .filter(n -> WALL_KIND_TARGET.equals(n.getNodeKind()))
                            .filter(n -> category.getNodeCode().equals(n.getParentCode()))
                            .map(n -> leafNode(n.getNodeCode(), n.getLabel()))
                            .collect(Collectors.toList()));
                    return node;
                })
                .collect(Collectors.toList());

        // 通道按「目标序 → 通道序」派生并按厂区分桶（与前端原生成顺序一致）
        Map<String, List<VideoWallGroupNode>> channelsByArea = new LinkedHashMap<>();
        Map<String, List<String>> cameraTargetMap = new LinkedHashMap<>();
        nodes.stream()
                .filter(n -> WALL_KIND_TARGET.equals(n.getNodeKind()))
                .forEach(target -> {
                    int seq = target.getSortNo() == null ? 0 : target.getSortNo();
                    int camCount = target.getCamCount() == null ? 0 : target.getCamCount();
                    List<VideoWallGroupNode> channels =
                            channelsByArea.computeIfAbsent(target.getAreaCode(), key -> new ArrayList<>());
                    for (int j = 1; j <= camCount; j++) {
                        String cameraId = "v-" + seq + "-" + j;
                        channels.add(leafNode(cameraId, String.format("CAM-装置#%03d-通道%d", seq, j)));
                        cameraTargetMap.put(cameraId, List.of(target.getNodeCode()));
                    }
                });

        List<VideoWallGroupNode> videoTree = nodes.stream()
                .filter(n -> WALL_KIND_AREA.equals(n.getNodeKind()))
                .map(area -> {
                    VideoWallGroupNode node = leafNode(area.getNodeCode(), area.getLabel());
                    List<VideoWallGroupNode> children = channelsByArea.get(area.getNodeCode());
                    if (children != null && !children.isEmpty()) {
                        node.setChildren(children);
                    }
                    return node;
                })
                .collect(Collectors.toList());

        List<VideoWallCamera> highArCameras = nodes.stream()
                .filter(n -> WALL_KIND_HIGH_AR.equals(n.getNodeKind()))
                .map(n -> {
                    VideoWallCamera camera = new VideoWallCamera();
                    camera.setId(n.getNodeCode());
                    camera.setLabel(n.getLabel());
                    return camera;
                })
                .collect(Collectors.toList());

        VideoWallNavigation nav = new VideoWallNavigation();
        nav.setTargetTree(targetTree);
        nav.setVideoTree(videoTree);
        nav.setCameraTargetMap(cameraTargetMap);
        nav.setDefaultHighAltitudeCameras(highArCameras);
        return nav;
    }

    private VideoWallGroupNode leafNode(String id, String label) {
        VideoWallGroupNode node = new VideoWallGroupNode();
        node.setId(id);
        node.setLabel(label);
        return node;
    }

    /**
     * 常驻视频监控分组（高空AR + 重点关注区域）。来自 V40 fac_video_important_group / _feed，
     * 取代前端 ImportantVideoPanel 硬编码的分组与通道（图像静态资源仍由前端按 image_key 映射）。
     */
    public ImportantVideoGroups importantGroups() {
        List<FacVideoImportantGroup> groups = importantGroupMapper.selectList(
                new LambdaQueryWrapper<FacVideoImportantGroup>().orderByAsc(FacVideoImportantGroup::getSortNo));
        List<FacVideoImportantFeed> feeds = importantFeedMapper.selectList(
                new LambdaQueryWrapper<FacVideoImportantFeed>().orderByAsc(FacVideoImportantFeed::getSortNo));
        Map<String, List<FacVideoImportantFeed>> feedsByGroup = feeds.stream()
                .collect(Collectors.groupingBy(FacVideoImportantFeed::getGroupCode));

        ImportantVideoGroups result = new ImportantVideoGroups();
        result.setHighArGroups(groups.stream()
                .filter(g -> "highAr".equals(g.getGroupType()))
                .map(g -> toImportantGroup(g, feedsByGroup))
                .collect(Collectors.toList()));
        result.setFocusGroups(groups.stream()
                .filter(g -> "focus".equals(g.getGroupType()))
                .map(g -> toImportantGroup(g, feedsByGroup))
                .collect(Collectors.toList()));
        return result;
    }

    private ImportantVideoGroup toImportantGroup(
            FacVideoImportantGroup group, Map<String, List<FacVideoImportantFeed>> feedsByGroup) {
        ImportantVideoGroup dto = new ImportantVideoGroup();
        dto.setId(group.getGroupCode());
        dto.setLabel(group.getGroupLabel());
        List<ImportantVideoFeed> feeds = (feedsByGroup.get(group.getGroupCode()) == null
                ? List.<FacVideoImportantFeed>of()
                : feedsByGroup.get(group.getGroupCode())).stream().map(f -> {
            ImportantVideoFeed feed = new ImportantVideoFeed();
            feed.setId(f.getFeedId());
            feed.setLabel(f.getFeedLabel());
            feed.setImageKey(f.getImageKey());
            feed.setPosition(f.getPosition());
            feed.setOnline(f.getOnline());
            return feed;
        }).collect(Collectors.toList());
        dto.setFeeds(feeds);
        return dto;
    }

    /** 摄像头分页（前端默认每页 9 宫格）。 */
    public VideoCameraPage cameras(int page, int size) {
        Page<FacVideoCamera> p = cameraMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<FacVideoCamera>().orderByAsc(FacVideoCamera::getSortNo));
        VideoCameraPage result = new VideoCameraPage();
        result.setTotal(p.getTotal());
        result.setPage(page);
        result.setSize(size);
        result.setPages((int) Math.ceil(p.getTotal() / (double) size));
        result.setList(p.getRecords().stream().map(this::toCameraItem).collect(Collectors.toList()));
        return result;
    }

    /** 摄像头静态截图字节（演示占位图）。无则返 null，由端点层转 404。 */
    public byte[] getSnapshotBytes(Long id) {
        FacVideoCamera camera = cameraMapper.selectById(id);
        return camera == null ? null : camera.getSnapshotBytes();
    }

    private VideoCameraItem toCameraItem(FacVideoCamera camera) {
        VideoCameraItem item = new VideoCameraItem();
        item.setId(camera.getId());
        item.setName(camera.getName());
        item.setCameraType(camera.getCameraType());
        item.setLocation(camera.getLocation());
        item.setStatus(camera.getStatusName());
        item.setHd(camera.getHd());
        item.setThumbIndex(camera.getThumbIndex());
        return item;
    }

    /** 视频联动配置列表。 */
    /**
     * 视频联动配置弹窗的四组下拉选项。
     * 相机名 / 相机类型由 fac_video_camera 派生；预置点 / 业务对象读 fac_video_linkage_option（V35）。
     */
    public VideoLinkageOptions linkageOptions() {
        List<FacVideoCamera> cameras = cameraMapper.selectList(new LambdaQueryWrapper<FacVideoCamera>()
                .orderByAsc(FacVideoCamera::getSortNo)
                .orderByAsc(FacVideoCamera::getId));
        VideoLinkageOptions options = new VideoLinkageOptions();
        options.setMonitorNames(cameras.stream()
                .map(FacVideoCamera::getName)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        options.setBusinessObjectCategories(cameras.stream()
                .map(FacVideoCamera::getCameraType)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        options.setPresetPoints(optionLabels("PRESET_POINT"));
        options.setBusinessObjects(optionLabels("BUSINESS_OBJECT"));
        return options;
    }

    private List<String> optionLabels(String optionType) {
        return linkageOptionMapper.selectList(new LambdaQueryWrapper<FacVideoLinkageOption>()
                        .eq(FacVideoLinkageOption::getOptionType, optionType)
                        .orderByAsc(FacVideoLinkageOption::getSortNo)).stream()
                .map(FacVideoLinkageOption::getOptionLabel)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<VideoLinkageItem> linkages() {
        return linkageMapper.selectList(new LambdaQueryWrapper<FacVideoLinkage>()
                        .orderByAsc(FacVideoLinkage::getSortNo)).stream()
                .map(this::toLinkageItem).collect(Collectors.toList());
    }

    /**
     * 保存视频联动配置（configCode 为空＝新建，非空＝更新）。
     *
     * <p>联动规则行整表替换：先删该 configCode 的旧行再按入参顺序重写 sort_no；
     * linkageCount / businessObjects 由 rules 推导。更新时未命中 configCode 返回 null（不抛异常）。</p>
     */
    public VideoLinkageItem saveLinkage(String configCode, VideoLinkageSaveRequest in) {
        List<VideoLinkageRuleInput> rules = in.getRules() == null ? List.of() : in.getRules();
        FacVideoLinkage linkage;
        if (configCode == null || configCode.isBlank()) {
            linkage = new FacVideoLinkage();
            linkage.setConfigCode(nextLinkageCode());
            linkage.setSortNo(nextLinkageSortNo());
            applyLinkageFields(linkage, in, rules);
            linkageMapper.insert(linkage);
        } else {
            linkage = findLinkage(configCode);
            if (linkage == null) {
                return null;
            }
            applyLinkageFields(linkage, in, rules);
            linkageMapper.updateById(linkage);
            linkageRuleMapper.delete(new LambdaQueryWrapper<FacVideoLinkageRule>()
                    .eq(FacVideoLinkageRule::getConfigCode, configCode));
        }
        int sortNo = 1;
        for (VideoLinkageRuleInput rule : rules) {
            FacVideoLinkageRule entity = new FacVideoLinkageRule();
            entity.setConfigCode(linkage.getConfigCode());
            entity.setPresetPoint(rule.getPresetPoint());
            entity.setObjectCategory(rule.getObjectCategory());
            entity.setObjectName(rule.getObjectName());
            entity.setSortNo(sortNo++);
            linkageRuleMapper.insert(entity);
        }
        return toLinkageItem(linkage);
    }

    /** 删除视频联动配置及其规则行；未命中 configCode 时 ok=false（不抛异常）。 */
    public DeleteResult deleteLinkage(String configCode) {
        DeleteResult result = new DeleteResult();
        FacVideoLinkage linkage = findLinkage(configCode);
        if (linkage == null) {
            result.setOk(false);
            return result;
        }
        linkageRuleMapper.delete(new LambdaQueryWrapper<FacVideoLinkageRule>()
                .eq(FacVideoLinkageRule::getConfigCode, configCode));
        result.setOk(linkageMapper.deleteById(linkage.getId()) > 0);
        return result;
    }

    private void applyLinkageFields(
            FacVideoLinkage linkage, VideoLinkageSaveRequest in, List<VideoLinkageRuleInput> rules) {
        linkage.setName(in.getName());
        linkage.setCode(in.getCode());
        linkage.setCategory(in.getCategory());
        linkage.setLinkageCount(rules.size());
        linkage.setBusinessObjects(rules.stream()
                .map(VideoLinkageRuleInput::getObjectName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .collect(Collectors.joining("、")));
    }

    private FacVideoLinkage findLinkage(String configCode) {
        if (configCode == null || configCode.isBlank()) {
            return null;
        }
        return linkageMapper.selectList(new LambdaQueryWrapper<FacVideoLinkage>()
                        .eq(FacVideoLinkage::getConfigCode, configCode))
                .stream().findFirst().orElse(null);
    }

    /** 生成下一个配置编码：取现有 lk-NNN 数字后缀最大值 +1；非数字后缀（如 UUID）不参与推算。 */
    private String nextLinkageCode() {
        int maxSeq = 0;
        for (FacVideoLinkage linkage : linkageMapper.selectList(new LambdaQueryWrapper<>())) {
            String code = linkage.getConfigCode();
            if (code == null || !code.startsWith("lk-")) {
                continue;
            }
            try {
                maxSeq = Math.max(maxSeq, Integer.parseInt(code.substring(3)));
            } catch (NumberFormatException ignored) {
                // 非数字编码跳过
            }
        }
        return String.format("lk-%03d", maxSeq + 1);
    }

    private int nextLinkageSortNo() {
        return linkageMapper.selectList(new LambdaQueryWrapper<FacVideoLinkage>()).stream()
                .map(FacVideoLinkage::getSortNo).filter(Objects::nonNull)
                .max(Integer::compareTo).orElse(0) + 1;
    }

    private VideoLinkageItem toLinkageItem(FacVideoLinkage linkage) {
        VideoLinkageItem item = new VideoLinkageItem();
        item.setId(linkage.getConfigCode());
        item.setName(linkage.getName());
        item.setCode(linkage.getCode());
        item.setCategory(linkage.getCategory());
        item.setLinkageCount(linkage.getLinkageCount());
        item.setBusinessObjects(linkage.getBusinessObjects());
        return item;
    }

    /** 联动规则；未预置规则的配置回退默认行（沿用前端 buildLinkageRules 的兜底语义）。 */
    public List<VideoLinkageRuleRow> linkageRules(String configCode) {
        List<FacVideoLinkageRule> rules = linkageRuleMapper.selectList(
                new LambdaQueryWrapper<FacVideoLinkageRule>()
                        .eq(FacVideoLinkageRule::getConfigCode, configCode)
                        .orderByAsc(FacVideoLinkageRule::getSortNo));
        if (rules.isEmpty()) {
            VideoLinkageRuleRow fallback = new VideoLinkageRuleRow();
            fallback.setId("r1");
            fallback.setPresetPoint("炼化厂区门口");
            fallback.setObjectCategory("摄像头");
            fallback.setObjectName("炼化厂区门口");
            return Collections.singletonList(fallback);
        }
        return rules.stream().map(r -> {
            VideoLinkageRuleRow row = new VideoLinkageRuleRow();
            row.setId("r" + r.getSortNo());
            row.setPresetPoint(r.getPresetPoint());
            row.setObjectCategory(r.getObjectCategory());
            row.setObjectName(r.getObjectName());
            return row;
        }).collect(Collectors.toList());
    }
}
