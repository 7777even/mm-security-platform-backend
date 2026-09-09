package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.BlacklistPersonItem;
import com.sinopec.mmsecurity.dto.BlacklistSummary;
import com.sinopec.mmsecurity.dto.BlacklistVehicleItem;
import com.sinopec.mmsecurity.entity.FacBlacklistEntry;
import com.sinopec.mmsecurity.mapper.FacBlacklistEntryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 安防黑名单服务。
 *
 * <p>数据来源为 V21 落地的 fac_blacklist_entry 真实表，取代前端硬编码的 blacklistMock。
 * 同一张表按 entry_kind 拆成车辆 / 人员两个列表，subject_name 分别映射为 plate 与 name。
 */
@Service
@RequiredArgsConstructor
public class BlacklistService {

    private static final String KIND_VEHICLE = "VEHICLE";

    private final FacBlacklistEntryMapper blacklistEntryMapper;

    /** 黑名单聚合：车辆列表 + 人员列表。 */
    public BlacklistSummary blacklist() {
        List<FacBlacklistEntry> entries = blacklistEntryMapper.selectList(
                new LambdaQueryWrapper<FacBlacklistEntry>().orderByAsc(FacBlacklistEntry::getSortNo));

        BlacklistSummary summary = new BlacklistSummary();
        summary.setVehicles(entries.stream()
                .filter(e -> KIND_VEHICLE.equals(e.getEntryKind()))
                .map(this::toVehicleItem).collect(Collectors.toList()));
        summary.setPersons(entries.stream()
                .filter(e -> !KIND_VEHICLE.equals(e.getEntryKind()))
                .map(this::toPersonItem).collect(Collectors.toList()));
        return summary;
    }

    private BlacklistVehicleItem toVehicleItem(FacBlacklistEntry entry) {
        BlacklistVehicleItem item = new BlacklistVehicleItem();
        item.setId(entry.getId());
        item.setPlate(entry.getSubjectName());
        item.setReason(entry.getReasonText());
        item.setTime(entry.getEventTime());
        item.setStatus(entry.getEntryStatus());
        return item;
    }

    private BlacklistPersonItem toPersonItem(FacBlacklistEntry entry) {
        BlacklistPersonItem item = new BlacklistPersonItem();
        item.setId(entry.getId());
        item.setName(entry.getSubjectName());
        item.setIdCard(entry.getIdCard());
        item.setReason(entry.getReasonText());
        item.setTime(entry.getEventTime());
        item.setStatus(entry.getEntryStatus());
        return item;
    }
}
