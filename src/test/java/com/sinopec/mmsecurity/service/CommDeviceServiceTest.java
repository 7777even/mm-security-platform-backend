package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.CommunicationDevice;
import com.sinopec.mmsecurity.dto.CommunicationDeviceGroups;
import com.sinopec.mmsecurity.dto.CommunicationGroup;
import com.sinopec.mmsecurity.entity.FacCommDevice;
import com.sinopec.mmsecurity.mapper.FacCommDeviceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
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
}
