package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.dto.VideoCameraItem;
import com.sinopec.mmsecurity.dto.VideoCameraPage;
import com.sinopec.mmsecurity.dto.VideoCategoryItem;
import com.sinopec.mmsecurity.dto.VideoGroupNode;
import com.sinopec.mmsecurity.dto.VideoLinkageItem;
import com.sinopec.mmsecurity.dto.VideoLinkageRuleRow;
import com.sinopec.mmsecurity.dto.VideoNavigation;
import com.sinopec.mmsecurity.entity.FacVideoCamera;
import com.sinopec.mmsecurity.entity.FacVideoGroup;
import com.sinopec.mmsecurity.entity.FacVideoLinkage;
import com.sinopec.mmsecurity.entity.FacVideoLinkageRule;
import com.sinopec.mmsecurity.mapper.FacVideoCameraMapper;
import com.sinopec.mmsecurity.mapper.FacVideoGroupMapper;
import com.sinopec.mmsecurity.mapper.FacVideoLinkageMapper;
import com.sinopec.mmsecurity.mapper.FacVideoLinkageRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    private final FacVideoGroupMapper groupMapper;
    private final FacVideoCameraMapper cameraMapper;
    private final FacVideoLinkageMapper linkageMapper;
    private final FacVideoLinkageRuleMapper linkageRuleMapper;

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
    public List<VideoLinkageItem> linkages() {
        return linkageMapper.selectList(new LambdaQueryWrapper<FacVideoLinkage>()
                        .orderByAsc(FacVideoLinkage::getSortNo)).stream()
                .map(l -> {
                    VideoLinkageItem item = new VideoLinkageItem();
                    item.setId(l.getConfigCode());
                    item.setName(l.getName());
                    item.setCode(l.getCode());
                    item.setCategory(l.getCategory());
                    item.setLinkageCount(l.getLinkageCount());
                    item.setBusinessObjects(l.getBusinessObjects());
                    return item;
                }).collect(Collectors.toList());
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
