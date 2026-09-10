package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.VideoLinkageItem;
import com.sinopec.mmsecurity.dto.VideoLinkageRuleInput;
import com.sinopec.mmsecurity.dto.VideoLinkageRuleRow;
import com.sinopec.mmsecurity.dto.VideoLinkageSaveRequest;
import com.sinopec.mmsecurity.dto.VideoNavigation;
import com.sinopec.mmsecurity.dto.VideoCameraPage;
import com.sinopec.mmsecurity.entity.FacVideoCamera;
import com.sinopec.mmsecurity.entity.FacVideoGroup;
import com.sinopec.mmsecurity.entity.FacVideoLinkage;
import com.sinopec.mmsecurity.entity.FacVideoLinkageRule;
import com.sinopec.mmsecurity.mapper.FacVideoCameraMapper;
import com.sinopec.mmsecurity.mapper.FacVideoGroupMapper;
import com.sinopec.mmsecurity.mapper.FacVideoLinkageMapper;
import com.sinopec.mmsecurity.mapper.FacVideoLinkageRuleMapper;
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
}
