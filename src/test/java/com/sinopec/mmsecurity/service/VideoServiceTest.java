package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.ImportantVideoGroups;
import com.sinopec.mmsecurity.dto.VideoCameraPage;
import com.sinopec.mmsecurity.dto.VideoLinkageItem;
import com.sinopec.mmsecurity.dto.VideoLinkageOptions;
import com.sinopec.mmsecurity.dto.VideoLinkageRuleInput;
import com.sinopec.mmsecurity.dto.VideoLinkageRuleRow;
import com.sinopec.mmsecurity.dto.VideoLinkageSaveRequest;
import com.sinopec.mmsecurity.dto.VideoNavigation;
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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * VideoService（纯 Mockito）：V40 常驻视频监控分组 + V14 导航/摄像头/联动 + V37 视频墙导航。
 * Mock 不执行 ORDER BY，所有 stub 均按 sort_no 升序预置（与 DB 返回顺序一致）。
 */
class VideoServiceTest {

    private final FacVideoGroupMapper groupMapper = mock(FacVideoGroupMapper.class);
    private final FacVideoCameraMapper cameraMapper = mock(FacVideoCameraMapper.class);
    private final FacVideoLinkageMapper linkageMapper = mock(FacVideoLinkageMapper.class);
    private final FacVideoLinkageRuleMapper linkageRuleMapper = mock(FacVideoLinkageRuleMapper.class);
    private final FacVideoLinkageOptionMapper linkageOptionMapper =
            mock(FacVideoLinkageOptionMapper.class);
    private final FacVideoWallNodeMapper wallNodeMapper = mock(FacVideoWallNodeMapper.class);
    private final FacVideoImportantGroupMapper importantGroupMapper =
            mock(FacVideoImportantGroupMapper.class);
    private final FacVideoImportantFeedMapper importantFeedMapper =
            mock(FacVideoImportantFeedMapper.class);
    private final VideoService service = new VideoService(groupMapper, cameraMapper, linkageMapper,
            linkageRuleMapper, linkageOptionMapper, wallNodeMapper,
            importantGroupMapper, importantFeedMapper);

    // ------------------------------------------------------------------ V14 导航

    @Test
    void navigation_buildsCategoriesAndTree() {
        FacVideoGroup cat1 = group("CATEGORY", "refining", "炼油区", 0, null, 1);
        FacVideoGroup root = group("TREE", "drill", "应急演练", 1, null, null);
        FacVideoGroup child = group("TREE", "drill-1", "演练1", 2, "drill", null);
        // navigation() 连续两次 selectList：先 CATEGORY，后 TREE
        when(groupMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(cat1), List.of(root, child));

        VideoNavigation nav = service.navigation();

        assertEquals(1, nav.getCategories().size());
        assertEquals("refining", nav.getCategories().get(0).getId());
        assertEquals("炼油区", nav.getCategories().get(0).getLabel());
        assertEquals(1, nav.getCategories().get(0).getIconType());
        assertEquals(1, nav.getTree().size());
        assertEquals("drill", nav.getTree().get(0).getId());
        assertEquals(1, nav.getTree().get(0).getChildren().size());
        assertEquals("drill-1", nav.getTree().get(0).getChildren().get(0).getId());
        // 叶子节点无 children 字段（DTO 层不输出）
        assertNull(nav.getTree().get(0).getChildren().get(0).getChildren());
    }

    // ------------------------------------------------------------------ V14 摄像头

    @Test
    void cameras_mapsPageFieldsAndItems() {
        FacVideoCamera cam = camera("炼油区-2", "球机", "中海壳牌", "loading", true, 3);
        Page<FacVideoCamera> page = new Page<>(1, 9);
        page.setTotal(10);
        page.setRecords(List.of(cam));
        doReturn(page).when(cameraMapper).selectPage(any(), any());

        VideoCameraPage result = service.cameras(1, 9);

        assertEquals(10, result.getTotal());
        assertEquals(1, result.getPage());
        assertEquals(9, result.getSize());
        assertEquals(2, result.getPages()); // ceil(10/9)
        assertEquals(1, result.getList().size());
        assertEquals("炼油区-2", result.getList().get(0).getName());
        assertEquals("球机", result.getList().get(0).getCameraType());
        assertEquals("loading", result.getList().get(0).getStatus());
        assertTrue(result.getList().get(0).getHd());
        assertEquals(3, result.getList().get(0).getThumbIndex());
    }

    @Test
    void snapshot_returnsBytesOrMissing() {
        FacVideoCamera cam = camera("炼油区-2", "球机", "中海壳牌", "live", true, 1);
        cam.setSnapshotBytes("jpeg-bytes".getBytes(StandardCharsets.UTF_8));
        when(cameraMapper.selectById(2L)).thenReturn(cam);
        when(cameraMapper.selectById(404L)).thenReturn(null);

        assertArrayEquals("jpeg-bytes".getBytes(StandardCharsets.UTF_8),
                service.getSnapshotBytes(2L));
        assertNull(service.getSnapshotBytes(404L));
    }

    // ------------------------------------------------------------------ V37 视频墙导航

    @Test
    void wallNavigation_buildsTreesMapsAndHighAr() {
        FacVideoWallNode category = wallNode("CATEGORY", "cat-1", "重大危险源", null, null, 1, null);
        FacVideoWallNode target = wallNode("TARGET", "t-1", "危化储罐区装置#001", "cat-1", "area-1", 2, 2);
        FacVideoWallNode area = wallNode("AREA", "area-1", "一号生产厂区", null, null, 3, null);
        FacVideoWallNode highAr = wallNode("HIGH_AR", "high-ar-1", "1#厂区高空AR·全景", null, null, 4, null);
        when(wallNodeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(category, target, area, highAr));

        VideoWallNavigation nav = service.wallNavigation();

        assertEquals(1, nav.getTargetTree().size());
        assertEquals("cat-1", nav.getTargetTree().get(0).getId());
        assertEquals(1, nav.getTargetTree().get(0).getChildren().size());
        assertEquals("t-1", nav.getTargetTree().get(0).getChildren().get(0).getId());

        // 通道由 TARGET 行 cam_count 派生（v-2-1 / v-2-2）并挂到 area-1 分区
        assertEquals(1, nav.getVideoTree().size());
        assertEquals(2, nav.getVideoTree().get(0).getChildren().size());
        assertEquals("v-2-1", nav.getVideoTree().get(0).getChildren().get(0).getId());
        assertEquals("CAM-装置#002-通道1", nav.getVideoTree().get(0).getChildren().get(0).getLabel());
        assertEquals(List.of("t-1"), nav.getCameraTargetMap().get("v-2-1"));

        assertEquals(1, nav.getDefaultHighAltitudeCameras().size());
        assertEquals("high-ar-1", nav.getDefaultHighAltitudeCameras().get(0).getId());
    }

    // ------------------------------------------------------------------ V14 联动配置

    @Test
    void linkages_mapsConfigRows() {
        when(linkageMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(linkage("lk-001", "门口联动", 2)));

        List<VideoLinkageItem> items = service.linkages();

        assertEquals(1, items.size());
        assertEquals("lk-001", items.get(0).getId());
        assertEquals("门口联动", items.get(0).getName());
        assertEquals(2, items.get(0).getLinkageCount());
    }

    @Test
    void linkageOptions_derivesCamerasAndReadsOptionTables() {
        FacVideoCamera camA = camera("炼油区门口", "球机", "厂区西门", "live", true, 1);
        FacVideoCamera camB = camera("罐区南侧", "枪机", "罐区", "ai", false, 2);
        FacVideoCamera camA2 = camera("炼油区门口", "球机", "厂区北门", "live", true, 3);
        when(cameraMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(camA, camB, camA2));
        when(linkageOptionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(
                List.of(option("PRESET_POINT", "炼化厂区门口", 1),
                        option("PRESET_POINT", "罐区高点", 2)),
                List.of(option("BUSINESS_OBJECT", "储罐", 1)));

        VideoLinkageOptions options = service.linkageOptions();

        assertEquals(List.of("炼油区门口", "罐区南侧"), options.getMonitorNames());
        assertEquals(List.of("球机", "枪机"), options.getBusinessObjectCategories());
        assertEquals(List.of("炼化厂区门口", "罐区高点"), options.getPresetPoints());
        assertEquals(List.of("储罐"), options.getBusinessObjects());
    }

    @Test
    void saveLinkage_createGeneratesCodeAndSortNo() {
        when(linkageMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(linkage("lk-002", "旧配置", 1))); // max lk-002 → lk-003, sortNo 2 → 3

        VideoLinkageSaveRequest in = request("新联动", "lk-x", "出入口", List.of(
                ruleInput("厂区西门", "摄像头", "西门球机"),
                ruleInput("罐区高点", "摄像头", " ")));

        VideoLinkageItem saved = service.saveLinkage(null, in);

        ArgumentCaptor<FacVideoLinkage> captor = ArgumentCaptor.forClass(FacVideoLinkage.class);
        verify(linkageMapper).insert(captor.capture());
        FacVideoLinkage inserted = captor.getValue();
        assertEquals("lk-003", inserted.getConfigCode());
        assertEquals(2, inserted.getSortNo());
        assertEquals(2, inserted.getLinkageCount());
        // 空白业务对象名不参与推导
        assertEquals("西门球机", inserted.getBusinessObjects());
        assertEquals("新联动", saved.getName());
        assertEquals("lk-003", saved.getId());
        verify(linkageRuleMapper, times(2)).insert(any(FacVideoLinkageRule.class));
    }

    @Test
    void saveLinkage_updateReplacesRules() {
        FacVideoLinkage existing = linkage("lk-001", "旧联动", 1);
        existing.setId(7L);
        when(linkageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(existing));

        VideoLinkageSaveRequest in = request("新联动", "lk-001", "出入口",
                List.of(ruleInput("厂区西门", "摄像头", "西门球机")));

        VideoLinkageItem saved = service.saveLinkage("lk-001", in);

        verify(linkageMapper).updateById(existing);
        verify(linkageRuleMapper).delete(any());
        verify(linkageRuleMapper, times(1)).insert(any(FacVideoLinkageRule.class));
        assertEquals("lk-001", saved.getId());
        assertEquals("新联动", saved.getName());
    }

    @Test
    void saveLinkage_updateMissReturnsNull() {
        when(linkageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        assertNull(service.saveLinkage("missing", request("x", "c", "cat", List.of())));
    }

    @Test
    void deleteLinkage_removesRulesAndConfig() {
        FacVideoLinkage existing = linkage("lk-001", "旧联动", 1);
        existing.setId(7L);
        when(linkageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(existing));
        when(linkageMapper.deleteById(7L)).thenReturn(1);

        DeleteResult result = service.deleteLinkage("lk-001");

        verify(linkageRuleMapper).delete(any());
        assertEquals(Boolean.TRUE, result.getOk());
    }

    @Test
    void deleteLinkage_missKeepsOkFalse() {
        when(linkageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        assertEquals(Boolean.FALSE, service.deleteLinkage("missing").getOk());
    }

    @Test
    void linkageRules_mapsRowsOrFallsBackToDefault() {
        FacVideoLinkageRule row = new FacVideoLinkageRule();
        row.setConfigCode("lk-001");
        row.setPresetPoint("厂区西门");
        row.setObjectCategory("摄像头");
        row.setObjectName("西门球机");
        row.setSortNo(2);
        when(linkageRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(row), List.of());

        List<VideoLinkageRuleRow> mapped = service.linkageRules("lk-001");
        assertEquals(1, mapped.size());
        assertEquals("r2", mapped.get(0).getId());
        assertEquals("西门球机", mapped.get(0).getObjectName());

        List<VideoLinkageRuleRow> fallback = service.linkageRules("lk-001");
        assertEquals(1, fallback.size());
        assertEquals("r1", fallback.get(0).getId());
        assertEquals("炼化厂区门口", fallback.get(0).getPresetPoint());
    }

    // ------------------------------------------------------------------ V40 常驻视频监控

    @Test
    void importantGroups_splitsByTypeAndAggregatesFeeds() {
        // Mock 不执行 ORDER BY，故按 sort_no 升序预置（与 DB 返回顺序一致）
        when(importantGroupMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                group("park", "园区全景组", "highAr", 1),
                group("refinery", "炼油区高点组", "highAr", 2),
                group("chemical", "化工区高点组", "highAr", 3),
                group("tank", "储罐区组", "focus", 4),
                group("device", "装置区组", "focus", 5),
                group("boundary", "厂界出入口组", "focus", 6)));
        when(importantFeedMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                feed("ar-park-1", "park", "园区北向全景", "highAr", "50% 30%", true, 1),
                feed("ar-park-2", "park", "炼油区全景", "highAr", "32% 50%", true, 2),
                feed("ar-refinery-1", "refinery", "一号高点西向", "highAr", "50% 30%", true, 1),
                feed("tank-1", "tank", "储罐区B-3东侧", "tanks", null, true, 1),
                feed("tank-2", "tank", "液化烃罐区南侧", "tanks", "60% 54%", true, 2),
                feed("device-1", "device", "催化裂化装置", "reactor", null, true, 1),
                feed("boundary-3", "boundary", "东侧厂界", "highAr", "78% 56%", true, 3),
                feed("boundary-4", "boundary", "南侧物流门", "highAr", "55% 82%", false, 4)));

        ImportantVideoGroups result = service.importantGroups();

        assertEquals(3, result.getHighArGroups().size());
        assertEquals(3, result.getFocusGroups().size());

        var park = result.getHighArGroups().get(0);
        assertEquals("park", park.getId());
        assertEquals("园区全景组", park.getLabel());
        assertEquals(2, park.getFeeds().size());
        assertEquals("ar-park-1", park.getFeeds().get(0).getId());
        assertEquals("highAr", park.getFeeds().get(0).getImageKey());
        assertEquals("50% 30%", park.getFeeds().get(0).getPosition());
        assertEquals(Boolean.TRUE, park.getFeeds().get(0).getOnline());
        assertEquals("炼油区高点组", result.getHighArGroups().get(1).getLabel());

        var tank = result.getFocusGroups().get(0);
        assertEquals("tank", tank.getId());
        assertEquals("储罐区组", tank.getLabel());
        assertEquals(2, tank.getFeeds().size());
        assertEquals("tanks", tank.getFeeds().get(0).getImageKey());
        assertNull(tank.getFeeds().get(0).getPosition());

        // 聚合只挂本组通道：boundary 组不含 device/tank 的行，且离线标志透传
        var boundary = result.getFocusGroups().get(2);
        assertEquals(2, boundary.getFeeds().size());
        assertEquals("boundary-3", boundary.getFeeds().get(0).getId());
        assertEquals("boundary-4", boundary.getFeeds().get(1).getId());
        assertEquals(Boolean.FALSE, boundary.getFeeds().get(1).getOnline());
    }

    @Test
    void importantGroups_groupWithoutFeedsYieldsEmptyList() {
        when(importantGroupMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(group("park", "园区全景组", "highAr", 1)));
        when(importantFeedMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        ImportantVideoGroups result = service.importantGroups();

        assertEquals(1, result.getHighArGroups().size());
        assertTrue(result.getHighArGroups().get(0).getFeeds().isEmpty());
        assertTrue(result.getFocusGroups().isEmpty());
    }

    @Test
    void importantGroups_handlesEmptyTables() {
        when(importantGroupMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(importantFeedMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        ImportantVideoGroups result = service.importantGroups();

        assertTrue(result.getHighArGroups().isEmpty());
        assertTrue(result.getFocusGroups().isEmpty());
    }

    @Test
    void importantGroups_ignoresUnknownGroupType() {
        when(importantGroupMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(group("other", "未知类型组", "other", 9)));
        when(importantFeedMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        ImportantVideoGroups result = service.importantGroups();

        assertTrue(result.getHighArGroups().isEmpty());
        assertTrue(result.getFocusGroups().isEmpty());
    }

    // ------------------------------------------------------------------ builders

    private static FacVideoGroup group(String kind, String code, String label,
            int sortNo, String parentCode, Integer iconIndex) {
        FacVideoGroup g = new FacVideoGroup();
        g.setGroupKind(kind);
        g.setNodeCode(code);
        g.setLabel(label);
        g.setSortNo(sortNo);
        g.setParentCode(parentCode);
        g.setIconIndex(iconIndex);
        return g;
    }

    private static FacVideoCamera camera(String name, String cameraType, String location,
            String status, boolean hd, int thumbIndex) {
        FacVideoCamera c = new FacVideoCamera();
        c.setId(2L);
        c.setName(name);
        c.setCameraType(cameraType);
        c.setLocation(location);
        c.setStatusName(status);
        c.setHd(hd);
        c.setThumbIndex(thumbIndex);
        c.setSortNo(1);
        return c;
    }

    private static FacVideoWallNode wallNode(String kind, String code, String label,
            String parentCode, String areaCode, int sortNo, Integer camCount) {
        FacVideoWallNode n = new FacVideoWallNode();
        n.setNodeKind(kind);
        n.setNodeCode(code);
        n.setLabel(label);
        n.setParentCode(parentCode);
        n.setAreaCode(areaCode);
        n.setSortNo(sortNo);
        n.setCamCount(camCount);
        return n;
    }

    private static FacVideoLinkage linkage(String code, String name, int count) {
        FacVideoLinkage l = new FacVideoLinkage();
        l.setConfigCode(code);
        l.setName(name);
        l.setCode("cfg-" + code);
        l.setCategory("出入口");
        l.setLinkageCount(count);
        l.setSortNo(1);
        return l;
    }

    private static FacVideoLinkageOption option(String type, String label, int sortNo) {
        FacVideoLinkageOption o = new FacVideoLinkageOption();
        o.setOptionType(type);
        o.setOptionLabel(label);
        o.setSortNo(sortNo);
        return o;
    }

    private static VideoLinkageSaveRequest request(String name, String code, String category,
            List<VideoLinkageRuleInput> rules) {
        VideoLinkageSaveRequest in = new VideoLinkageSaveRequest();
        in.setName(name);
        in.setCode(code);
        in.setCategory(category);
        in.setRules(rules);
        return in;
    }

    private static VideoLinkageRuleInput ruleInput(String point, String category, String name) {
        VideoLinkageRuleInput r = new VideoLinkageRuleInput();
        r.setPresetPoint(point);
        r.setObjectCategory(category);
        r.setObjectName(name);
        return r;
    }

    private static FacVideoImportantGroup group(
            String code, String label, String type, int sortNo) {
        FacVideoImportantGroup g = new FacVideoImportantGroup();
        g.setGroupCode(code);
        g.setGroupLabel(label);
        g.setGroupType(type);
        g.setSortNo(sortNo);
        return g;
    }

    private static FacVideoImportantFeed feed(
            String id, String groupCode, String label, String imageKey,
            String position, boolean online, int sortNo) {
        FacVideoImportantFeed f = new FacVideoImportantFeed();
        f.setFeedId(id);
        f.setGroupCode(groupCode);
        f.setFeedLabel(label);
        f.setImageKey(imageKey);
        f.setPosition(position);
        f.setOnline(online);
        f.setSortNo(sortNo);
        return f;
    }
}
