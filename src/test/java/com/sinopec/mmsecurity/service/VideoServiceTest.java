package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.VideoLinkageItem;
import com.sinopec.mmsecurity.dto.VideoLinkageRuleInput;
import com.sinopec.mmsecurity.dto.VideoLinkageRuleRow;
import com.sinopec.mmsecurity.dto.VideoLinkageSaveRequest;
import com.sinopec.mmsecurity.dto.VideoNavigation;
import com.sinopec.mmsecurity.dto.VideoCameraPage;
import com.sinopec.mmsecurity.dto.VideoWallGroupNode;
import com.sinopec.mmsecurity.dto.VideoWallNavigation;
import com.sinopec.mmsecurity.entity.FacVideoCamera;
import com.sinopec.mmsecurity.entity.FacVideoGroup;
import com.sinopec.mmsecurity.entity.FacVideoLinkage;
import com.sinopec.mmsecurity.entity.FacVideoLinkageRule;
import com.sinopec.mmsecurity.entity.FacVideoWallNode;
import com.sinopec.mmsecurity.mapper.FacVideoCameraMapper;
import com.sinopec.mmsecurity.mapper.FacVideoGroupMapper;
import com.sinopec.mmsecurity.mapper.FacVideoLinkageMapper;
import com.sinopec.mmsecurity.mapper.FacVideoLinkageRuleMapper;
import com.sinopec.mmsecurity.mapper.FacVideoWallNodeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 视频控制服务逻辑校验（纯 Mockito，不起 Spring 上下文、不连 DB）。 */
@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private FacVideoGroupMapper groupMapper;
    @Mock
    private FacVideoCameraMapper cameraMapper;
    @Mock
    private FacVideoLinkageMapper linkageMapper;
    @Mock
    private FacVideoLinkageRuleMapper linkageRuleMapper;
    @Mock
    private FacVideoWallNodeMapper wallNodeMapper;

    @InjectMocks
    private VideoService service;

    private static FacVideoGroup group(String nodeCode, String label, String parentCode, String kind, int sortNo) {
        FacVideoGroup g = new FacVideoGroup();
        g.setNodeCode(nodeCode);
        g.setLabel(label);
        g.setParentCode(parentCode);
        g.setGroupKind(kind);
        g.setIconIndex(0);
        g.setSortNo(sortNo);
        return g;
    }

    @Test
    void navigation_buildsCategoriesAndNestedTree() {
        FacVideoGroup category = group("refining", "炼油区", null, "CATEGORY", 1);
        FacVideoGroup root = group("drill", "应急演练", null, "TREE", 1);
        FacVideoGroup child = group("drill-1", "演练1", "drill", "TREE", 1);
        when(groupMapper.selectList(any())).thenReturn(List.of(category), List.of(root, child));

        VideoNavigation nav = service.navigation();

        assertEquals(1, nav.getCategories().size());
        assertEquals("refining", nav.getCategories().get(0).getId());
        assertEquals(1, nav.getTree().size());
        assertEquals("drill", nav.getTree().get(0).getId());
        assertEquals(1, nav.getTree().get(0).getChildren().size());
        assertEquals("演练1", nav.getTree().get(0).getChildren().get(0).getLabel());
    }

    @Test
    void cameras_mapsPageAndStatus() {
        FacVideoCamera camera = new FacVideoCamera();
        camera.setId(2L);
        camera.setName("炼油区-2");
        camera.setCameraType("球机");
        camera.setLocation("中海壳牌石油化工有限公司");
        camera.setStatusName("loading");
        camera.setHd(true);
        camera.setThumbIndex(1);
        Page<FacVideoCamera> page = new Page<>(1, 9);
        page.setTotal(27);
        page.setRecords(List.of(camera));
        when(cameraMapper.selectPage(any(), any())).thenReturn(page);

        VideoCameraPage result = service.cameras(1, 9);

        assertEquals(27, result.getTotal());
        assertEquals(3, result.getPages());
        assertEquals(1, result.getList().size());
        assertEquals("loading", result.getList().get(0).getStatus());
        assertTrue(result.getList().get(0).getHd());
    }

    @Test
    void linkageRules_mapsSortNoToRowId() {
        FacVideoLinkageRule rule = new FacVideoLinkageRule();
        rule.setConfigCode("lk-001");
        rule.setPresetPoint("石脑油罐区-东南角");
        rule.setObjectCategory("重大危险源");
        rule.setObjectName("石脑油罐区");
        rule.setSortNo(1);
        when(linkageRuleMapper.selectList(any())).thenReturn(List.of(rule));

        List<VideoLinkageRuleRow> rows = service.linkageRules("lk-001");

        assertEquals(1, rows.size());
        assertEquals("r1", rows.get(0).getId());
        assertEquals("石脑油罐区-东南角", rows.get(0).getPresetPoint());
    }

    @Test
    void linkageRules_fallsBackToDefaultRowWhenEmpty() {
        when(linkageRuleMapper.selectList(any())).thenReturn(List.of());

        List<VideoLinkageRuleRow> rows = service.linkageRules("lk-099");

        assertEquals(1, rows.size());
        assertEquals("r1", rows.get(0).getId());
        assertEquals("炼化厂区门口", rows.get(0).getPresetPoint());
        assertEquals("摄像头", rows.get(0).getObjectCategory());
    }

    @Test
    void linkages_mapsConfigCodeToId() {
        FacVideoLinkage linkage = new FacVideoLinkage();
        linkage.setConfigCode("lk-001");
        linkage.setName("XX强3-2棚伯");
        linkage.setCode("HKJK-5124863");
        linkage.setCategory("枪机");
        linkage.setLinkageCount(4);
        linkage.setBusinessObjects("石脑油罐区、催化裂化装置");
        when(linkageMapper.selectList(any())).thenReturn(List.of(linkage));

        List<VideoLinkageItem> items = service.linkages();

        assertEquals(1, items.size());
        assertEquals("lk-001", items.get(0).getId());
        assertEquals(4, items.get(0).getLinkageCount());
        assertEquals("石脑油罐区、催化裂化装置", items.get(0).getBusinessObjects());
    }

    @Test
    void saveLinkage_createsWithGeneratedCodeAndDerivedCounters() {
        when(linkageMapper.selectList(any())).thenReturn(List.of());
        VideoLinkageSaveRequest req = new VideoLinkageSaveRequest();
        req.setName("新监控");
        req.setCode("HKJK-9999999");
        req.setCategory("球机");
        req.setRules(List.of(
                ruleInput("P1", "储罐", "储油罐区"),
                ruleInput("P2", "储罐", "储油罐区"),
                ruleInput("P3", "生产装置", "催化裂化装置")));

        VideoLinkageItem item = service.saveLinkage(null, req);

        assertEquals("lk-001", item.getId());
        assertEquals(3, item.getLinkageCount());
        assertEquals("储油罐区、催化裂化装置", item.getBusinessObjects());
        verify(linkageMapper).insert(any(FacVideoLinkage.class));
        verify(linkageRuleMapper, times(3)).insert(any(FacVideoLinkageRule.class));
    }

    @Test
    void saveLinkage_updatesExistingAndReplacesRules() {
        FacVideoLinkage existing = new FacVideoLinkage();
        existing.setId(6L);
        existing.setConfigCode("lk-006");
        existing.setSortNo(6);
        when(linkageMapper.selectList(any())).thenReturn(List.of(existing));
        VideoLinkageSaveRequest req = new VideoLinkageSaveRequest();
        req.setName("改后名称");
        req.setCode("HKJK-1");
        req.setCategory("枪机");
        req.setRules(List.of());

        VideoLinkageItem item = service.saveLinkage("lk-006", req);

        assertEquals("lk-006", item.getId());
        assertEquals(0, item.getLinkageCount());
        assertEquals("", item.getBusinessObjects());
        verify(linkageMapper).updateById(any(FacVideoLinkage.class));
        verify(linkageRuleMapper).delete(any());
    }

    @Test
    void saveLinkage_returnsNullWhenConfigCodeMissing() {
        when(linkageMapper.selectList(any())).thenReturn(List.of());

        assertNull(service.saveLinkage("lk-404", new VideoLinkageSaveRequest()));
    }

    @Test
    void deleteLinkage_removesConfigAndRules() {
        FacVideoLinkage existing = new FacVideoLinkage();
        existing.setId(6L);
        existing.setConfigCode("lk-006");
        when(linkageMapper.selectList(any())).thenReturn(List.of(existing));
        when(linkageMapper.deleteById(6L)).thenReturn(1);

        DeleteResult result = service.deleteLinkage("lk-006");

        assertTrue(result.getOk());
        verify(linkageRuleMapper).delete(any());
    }

    @Test
    void deleteLinkage_returnsFalseWhenMissing() {
        when(linkageMapper.selectList(any())).thenReturn(List.of());

        assertFalse(service.deleteLinkage("lk-404").getOk());
    }

    private static VideoLinkageRuleInput ruleInput(String preset, String category, String name) {
        VideoLinkageRuleInput input = new VideoLinkageRuleInput();
        input.setPresetPoint(preset);
        input.setObjectCategory(category);
        input.setObjectName(name);
        return input;
    }

    @Test
    void wallNavigation_buildsTreesDerivesChannelsAndMap() {
        // 顺序模拟 DB 按 sort_no 升序返回
        FacVideoWallNode cat = wallNode("cat-1", "CATEGORY", "重大危险源", null, null, 0, 1);
        FacVideoWallNode area1 = wallNode("area-1", "AREA", "一号生产厂区", null, null, 0, 1);
        FacVideoWallNode area2 = wallNode("area-2", "AREA", "二号生产厂区", null, null, 0, 2);
        FacVideoWallNode highAr = wallNode("high-ar-1", "HIGH_AR", "1#厂区高空AR·全景", null, null, 0, 1);
        FacVideoWallNode t1 = wallNode("t-1", "TARGET", "危化储罐区装置#001", "cat-1", "area-1", 5, 1);
        FacVideoWallNode t2 = wallNode("t-2", "TARGET", "危化储罐区装置#002", "cat-1", "area-2", 2, 2);
        when(wallNodeMapper.selectList(any()))
                .thenReturn(List.of(cat, area1, area2, highAr, t1, t2));

        VideoWallNavigation nav = service.wallNavigation();

        // 目标树：分类 → 目标
        assertEquals(1, nav.getTargetTree().size());
        assertEquals("cat-1", nav.getTargetTree().get(0).getId());
        assertEquals(2, nav.getTargetTree().get(0).getChildren().size());
        assertEquals("危化储罐区装置#001", nav.getTargetTree().get(0).getChildren().get(0).getLabel());

        // 厂区视频目录：每目标按 cam_count 派生通道，编码 v-{i}-{j}、命名 CAM-装置#{i:003}-通道{j}
        assertEquals(2, nav.getVideoTree().size());
        VideoWallGroupNode firstArea = nav.getVideoTree().get(0);
        assertEquals("area-1", firstArea.getId());
        assertEquals(5, firstArea.getChildren().size());
        assertEquals("v-1-1", firstArea.getChildren().get(0).getId());
        assertEquals("CAM-装置#001-通道1", firstArea.getChildren().get(0).getLabel());
        assertEquals("v-1-5", firstArea.getChildren().get(4).getId());
        VideoWallGroupNode secondArea = nav.getVideoTree().get(1);
        assertEquals(2, secondArea.getChildren().size());
        assertEquals("v-2-2", secondArea.getChildren().get(1).getId());

        // 通道 → 目标映射（1:1），总数 = 各目标 cam_count 之和
        assertEquals(List.of("t-1"), nav.getCameraTargetMap().get("v-1-3"));
        assertEquals(List.of("t-2"), nav.getCameraTargetMap().get("v-2-1"));
        assertEquals(7, nav.getCameraTargetMap().size());

        // 默认高空AR相机
        assertEquals(1, nav.getDefaultHighAltitudeCameras().size());
        assertEquals("high-ar-1", nav.getDefaultHighAltitudeCameras().get(0).getId());
        assertEquals("1#厂区高空AR·全景", nav.getDefaultHighAltitudeCameras().get(0).getLabel());
    }

    @Test
    void wallNavigation_emptyTableReturnsEmptyStructure() {
        when(wallNodeMapper.selectList(any())).thenReturn(List.of());

        VideoWallNavigation nav = service.wallNavigation();

        assertTrue(nav.getTargetTree().isEmpty());
        assertTrue(nav.getVideoTree().isEmpty());
        assertTrue(nav.getCameraTargetMap().isEmpty());
        assertTrue(nav.getDefaultHighAltitudeCameras().isEmpty());
    }

    private static FacVideoWallNode wallNode(String code, String kind, String label,
                                             String parentCode, String areaCode, int camCount, int sortNo) {
        FacVideoWallNode node = new FacVideoWallNode();
        node.setNodeCode(code);
        node.setNodeKind(kind);
        node.setLabel(label);
        node.setParentCode(parentCode);
        node.setAreaCode(areaCode);
        node.setCamCount(camCount);
        node.setSortNo(sortNo);
        return node;
    }
}
