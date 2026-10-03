package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.CommDeviceWriteRequest;
import com.sinopec.mmsecurity.dto.CommunicationDevice;
import com.sinopec.mmsecurity.dto.CommunicationDeviceGroups;
import com.sinopec.mmsecurity.dto.CommunicationGroup;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.entity.FacCommDevice;
import com.sinopec.mmsecurity.mapper.FacCommDeviceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 通讯设备服务逻辑校验（纯 Mockito，不起 Spring 上下文、不连 DB）。 */
@ExtendWith(MockitoExtension.class)
class CommDeviceServiceTest {

    @Mock
    private FacCommDeviceMapper commDeviceMapper;

    @InjectMocks
    private CommDeviceService service;

    private static FacCommDevice device(String code, String type, String groupKey, String groupLabel,
                                        String name, int sortNo) {
        FacCommDevice entity = new FacCommDevice();
        entity.setId((long) sortNo);
        entity.setDeviceCode(code);
        entity.setDeviceType(type);
        entity.setGroupKey(groupKey);
        entity.setGroupLabel(groupLabel);
        entity.setDeviceName(name);
        entity.setAreaName("A装置区");
        entity.setLocationName("A装置区东北角");
        entity.setDeviceStatus("在线");
        entity.setLongitude(110.881);
        entity.setLatitude(21.671);
        entity.setCategoryName("室外防爆广播");
        entity.setInstallTime("2024-03-12");
        entity.setOwnerName("安环部");
        entity.setIpAddress("10.20.31.101");
        entity.setLastCheckTime("2026-08-10 08:30:00");
        entity.setSortNo(sortNo);
        return entity;
    }

    @Test
    void groups_splitsThreeTabsAndKeepsGroupOrder() {
        when(commDeviceMapper.selectList(any())).thenReturn(List.of(
                device("bc-a1", "broadcast", "area-a", "A装置区 (6)", "A装置区1#广播", 1),
                device("bc-p1", "broadcast", "public", "公共区 (1)", "厂区大门广播", 2),
                device("ph-a1", "phone", "area-a", "A装置区 (2)", "A装置区1#电话", 3),
                device("ic-a1", "intercom", "area-a", "A装置区 (2)", "A装置区1#对讲", 4)));

        CommunicationDeviceGroups groups = service.groups();

        assertEquals(2, groups.getBroadcast().size());
        assertEquals(1, groups.getPhone().size());
        assertEquals(1, groups.getIntercom().size());
        CommunicationGroup broadcastFirst = groups.getBroadcast().get(0);
        assertEquals("area-a", broadcastFirst.getKey());
        assertEquals("A装置区 (6)", broadcastFirst.getLabel());
        assertEquals("bc-a1", broadcastFirst.getDevices().get(0).getId());
        assertEquals("public", groups.getBroadcast().get(1).getKey());
        assertEquals("ph-a1", groups.getPhone().get(0).getDevices().get(0).getId());
        assertEquals("ic-a1", groups.getIntercom().get(0).getDevices().get(0).getId());
    }

    @Test
    void byCode_mapsDeviceDetailFields() {
        when(commDeviceMapper.selectList(any())).thenReturn(List.of(
                device("bc-a1", "broadcast", "area-a", "A装置区 (6)", "A装置区1#广播", 1)));

        CommunicationDevice device = service.byCode("bc-a1");

        assertEquals("bc-a1", device.getId());
        assertEquals("broadcast", device.getType());
        assertEquals("在线", device.getStatus());
        assertEquals(Double.valueOf(110.881), device.getLongitude());
        assertEquals("室外防爆广播", device.getDetail().getCategory());
        assertEquals("2024-03-12", device.getDetail().getInstallTime());
        assertEquals("安环部", device.getDetail().getOwner());
        assertEquals("10.20.31.101", device.getDetail().getIp());
        assertEquals("2026-08-10 08:30:00", device.getDetail().getLastCheck());
    }

    @Test
    void byCode_returnsNullWhenMissing() {
        when(commDeviceMapper.selectList(any())).thenReturn(List.of());

        assertNull(service.byCode("not-exist"));
    }

    // ---- 写端点（communication.device）----
    // 主线：① id / sort_no 走 LedgerIdSupport 的 max+1（避三方言自增序列滞后撞主键 409）；
    // ② deviceCode 是「按业务自然键定位」的字段，更新时不得被请求体改写。

    private static CommDeviceWriteRequest writeRequest(String code) {
        CommDeviceWriteRequest in = new CommDeviceWriteRequest();
        in.setDeviceCode(code);
        in.setDeviceType("broadcast");
        in.setGroupKey("area-a");
        in.setGroupLabel("A装置区 (6)");
        in.setDeviceName("A装置区1#广播");
        in.setAreaName("A装置区");
        in.setLocationName("A装置区东北角");
        in.setDeviceStatus("在线");
        in.setLongitude(110.881);
        in.setLatitude(21.671);
        in.setCategoryName("室外防爆广播");
        in.setInstallTime("2024-03-12");
        in.setOwnerName("安环部");
        in.setIpAddress("10.20.31.101");
        in.setLastCheckTime("2026-08-10 08:30:00");
        return in;
    }

    @Test
    void createDevice_emptyTable_assignsIdOneSortNoOneAndVersionZero() {
        when(commDeviceMapper.selectOne(any())).thenReturn(null);

        CommunicationDevice created = service.createDevice(writeRequest("bc-a9"));

        assertEquals("bc-a9", created.getId());
        ArgumentCaptor<FacCommDevice> captor = ArgumentCaptor.forClass(FacCommDevice.class);
        verify(commDeviceMapper).insert(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(1, captor.getValue().getSortNo());
        assertEquals(0L, captor.getValue().getVersion());
    }

    @Test
    void createDevice_existingRows_assignsMaxIdAndMaxSortNoPlusOne() {
        when(commDeviceMapper.selectOne(any())).thenReturn(
                device("bc-a1", "broadcast", "area-a", "A装置区 (6)", "A装置区1#广播", 7));

        service.createDevice(writeRequest("bc-a9"));

        ArgumentCaptor<FacCommDevice> captor = ArgumentCaptor.forClass(FacCommDevice.class);
        verify(commDeviceMapper).insert(captor.capture());
        assertEquals(8L, captor.getValue().getId());
        assertEquals(8, captor.getValue().getSortNo());
    }

    @Test
    void updateDevice_bodyDeviceCodeDoesNotOverridePathKey() {
        // PUT /devices/{deviceCode}：定位的业务自然键必须由路径决定。
        // 若被请求体覆盖，body 与 path 不一致时会把设备改到另一个编码下，资源从原 URL 消失。
        FacCommDevice existing = device("bc-a1", "broadcast", "area-a", "A装置区 (6)", "A装置区1#广播", 1);
        when(commDeviceMapper.selectList(any())).thenReturn(List.of(existing));

        service.updateDevice("bc-a1", writeRequest("bc-OTHER"));

        assertEquals("bc-a1", existing.getDeviceCode());
    }

    @Test
    void updateDevice_appliesFieldsWhenFound() {
        FacCommDevice existing = device("bc-a1", "broadcast", "area-a", "A装置区 (6)", "A装置区1#广播", 1);
        when(commDeviceMapper.selectList(any())).thenReturn(List.of(existing));
        CommDeviceWriteRequest in = writeRequest("bc-a1");
        in.setDeviceStatus("离线");
        in.setDeviceName("改名后广播");

        CommunicationDevice updated = service.updateDevice("bc-a1", in);

        assertEquals("离线", updated.getStatus());
        assertEquals("改名后广播", updated.getName());
        verify(commDeviceMapper).updateById(existing);
    }

    @Test
    void updateDevice_notFound_returnsNull() {
        when(commDeviceMapper.selectList(any())).thenReturn(List.of());

        assertNull(service.updateDevice("not-exist", writeRequest("not-exist")));
    }

    @Test
    void deleteDevice_present_returnsOkTrueAndDeletesByPrimaryKey() {
        // 删除按实体主键 id 下发，而非按 deviceCode 字符串。
        when(commDeviceMapper.selectList(any())).thenReturn(List.of(
                device("bc-a1", "broadcast", "area-a", "A装置区 (6)", "A装置区1#广播", 1)));
        when(commDeviceMapper.deleteById(1L)).thenReturn(1);

        DeleteResult result = service.deleteDevice("bc-a1");

        assertTrue(result.getOk());
    }

    @Test
    void deleteDevice_absent_returnsOkFalseAndSkipsDelete() {
        when(commDeviceMapper.selectList(any())).thenReturn(List.of());

        assertFalse(service.deleteDevice("not-exist").getOk());
        verify(commDeviceMapper, never()).deleteById(anyLong());
    }
}
