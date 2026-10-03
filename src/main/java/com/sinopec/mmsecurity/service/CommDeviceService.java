package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.annotation.RealtimeSync;
import com.sinopec.mmsecurity.dto.CommunicationDevice;
import com.sinopec.mmsecurity.dto.CommunicationDeviceDetail;
import com.sinopec.mmsecurity.dto.CommDeviceWriteRequest;
import com.sinopec.mmsecurity.dto.CommunicationDeviceGroups;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.CommunicationGroup;
import com.sinopec.mmsecurity.entity.FacCommDevice;
import com.sinopec.mmsecurity.mapper.FacCommDeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通讯设备服务（广播 / 电话 / 对讲）。
 *
 * <p>数据来源为 V22 落地的 fac_comm_device 真实表，取代前端硬编码的 communicationDeviceMock。
 * 三个页签的分组在库里以 device_type 区分，一次查询后按类型拆分。
 */
@Service
@RequiredArgsConstructor
public class CommDeviceService {

    private static final String TYPE_BROADCAST = "broadcast";
    private static final String TYPE_PHONE = "phone";
    private static final String TYPE_INTERCOM = "intercom";

    private final FacCommDeviceMapper commDeviceMapper;

    /** 三个页签的分组集合：broadcast / phone / intercom。 */
    public CommunicationDeviceGroups groups() {
        List<FacCommDevice> devices = commDeviceMapper.selectList(
                new LambdaQueryWrapper<FacCommDevice>()
                        .orderByAsc(FacCommDevice::getDeviceType, FacCommDevice::getSortNo));
        CommunicationDeviceGroups result = new CommunicationDeviceGroups();
        result.setBroadcast(groupByType(devices, TYPE_BROADCAST));
        result.setPhone(groupByType(devices, TYPE_PHONE));
        result.setIntercom(groupByType(devices, TYPE_INTERCOM));
        return result;
    }

    /** 按设备编码查单台设备，未命中返回 null（由上层按空数据处理）。 */
    public CommunicationDevice byCode(String deviceCode) {
        List<FacCommDevice> devices = commDeviceMapper.selectList(
                new LambdaQueryWrapper<FacCommDevice>()
                        .eq(FacCommDevice::getDeviceCode, deviceCode));
        return devices.stream().findFirst().map(this::toDevice).orElse(null);
    }

    /** 新建通讯设备台账；id 与 sort_no 由 LedgerIdSupport 分配，广播 communication.device 实时通道。 */
    @RealtimeSync(domain = "communication.device")
    @Transactional
    public CommunicationDevice createDevice(CommDeviceWriteRequest in) {
        FacCommDevice device = new FacCommDevice();
        device.setId(LedgerIdSupport.nextId(commDeviceMapper, FacCommDevice::getId, FacCommDevice::getId));
        device.setSortNo(LedgerIdSupport.nextSortNo(commDeviceMapper, FacCommDevice::getSortNo, FacCommDevice::getSortNo));
        applyDeviceFields(device, in);
        device.setVersion(0L);
        commDeviceMapper.insert(device);
        return toDevice(device);
    }

    /** 更新通讯设备台账（按 deviceCode）；未命中返回 null。广播 communication.device 实时通道。 */
    @RealtimeSync(domain = "communication.device")
    @Transactional
    public CommunicationDevice updateDevice(String deviceCode, CommDeviceWriteRequest in) {
        FacCommDevice device = findByCode(deviceCode);
        if (device == null) {
            return null;
        }
        applyDeviceFields(device, in);
        commDeviceMapper.updateById(device);
        return toDevice(device);
    }

    /** 删除通讯设备台账（按 deviceCode）；未命中 ok=false。广播 communication.device 实时通道。 */
    @RealtimeSync(domain = "communication.device")
    @Transactional
    public DeleteResult deleteDevice(String deviceCode) {
        DeleteResult result = new DeleteResult();
        FacCommDevice device = findByCode(deviceCode);
        if (device == null) {
            result.setOk(false);
            return result;
        }
        result.setOk(commDeviceMapper.deleteById(device.getId()) > 0);
        return result;
    }

    private FacCommDevice findByCode(String deviceCode) {
        return commDeviceMapper.selectList(
                        new LambdaQueryWrapper<FacCommDevice>()
                                .eq(FacCommDevice::getDeviceCode, deviceCode))
                .stream().findFirst().orElse(null);
    }

    private void applyDeviceFields(FacCommDevice device, CommDeviceWriteRequest in) {
        device.setDeviceCode(in.getDeviceCode());
        device.setDeviceType(in.getDeviceType());
        device.setGroupKey(in.getGroupKey());
        device.setGroupLabel(in.getGroupLabel());
        device.setDeviceName(in.getDeviceName());
        device.setAreaName(in.getAreaName());
        device.setLocationName(in.getLocationName());
        device.setDeviceStatus(in.getDeviceStatus());
        device.setLongitude(in.getLongitude());
        device.setLatitude(in.getLatitude());
        device.setCategoryName(in.getCategoryName());
        device.setInstallTime(in.getInstallTime());
        device.setOwnerName(in.getOwnerName());
        device.setIpAddress(in.getIpAddress());
        device.setLastCheckTime(in.getLastCheckTime());
    }

    private List<CommunicationGroup> groupByType(List<FacCommDevice> devices, String deviceType) {
        Map<String, List<FacCommDevice>> byKey = new LinkedHashMap<>();
        devices.stream()
                .filter(d -> deviceType.equals(d.getDeviceType()))
                .forEach(d -> byKey.computeIfAbsent(d.getGroupKey(), k -> new ArrayList<>()).add(d));
        List<CommunicationGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<FacCommDevice>> entry : byKey.entrySet()) {
            CommunicationGroup group = new CommunicationGroup();
            group.setKey(entry.getKey());
            group.setLabel(entry.getValue().get(0).getGroupLabel());
            group.setDevices(entry.getValue().stream().map(this::toDevice).collect(Collectors.toList()));
            groups.add(group);
        }
        return groups;
    }

    private CommunicationDevice toDevice(FacCommDevice entity) {
        CommunicationDevice device = new CommunicationDevice();
        device.setId(entity.getDeviceCode());
        device.setType(entity.getDeviceType());
        device.setName(entity.getDeviceName());
        device.setArea(entity.getAreaName());
        device.setLocation(entity.getLocationName());
        device.setStatus(entity.getDeviceStatus());
        device.setLongitude(entity.getLongitude());
        device.setLatitude(entity.getLatitude());
        device.setDetail(toDetail(entity));
        return device;
    }

    private CommunicationDeviceDetail toDetail(FacCommDevice entity) {
        CommunicationDeviceDetail detail = new CommunicationDeviceDetail();
        detail.setCategory(entity.getCategoryName());
        detail.setInstallTime(entity.getInstallTime());
        detail.setOwner(entity.getOwnerName());
        detail.setIp(entity.getIpAddress());
        detail.setLastCheck(entity.getLastCheckTime());
        return detail;
    }
}
