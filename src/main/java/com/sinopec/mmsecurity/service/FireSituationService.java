package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.FireSituationMarkerItem;
import com.sinopec.mmsecurity.dto.FireSituationMarkerSummary;
import com.sinopec.mmsecurity.entity.FacFireSituationMarker;
import com.sinopec.mmsecurity.mapper.FacFireSituationMarkerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 火情态势地图点位服务。
 *
 * <p>数据来源为 V21 落地的 fac_fire_situation_marker 真实表，取代前端硬编码的
 * fireSituationMapMock。DTO 的 id 由 marker_code 映射（字符串，如 event-1）。
 */
@Service
@RequiredArgsConstructor
public class FireSituationService {

    private final FacFireSituationMarkerMapper fireSituationMarkerMapper;

    /** 地图聚合点位列表（按 sort_no 升序）。 */
    public FireSituationMarkerSummary markers() {
        List<FacFireSituationMarker> markers = fireSituationMarkerMapper.selectList(
                new LambdaQueryWrapper<FacFireSituationMarker>().orderByAsc(FacFireSituationMarker::getSortNo));

        FireSituationMarkerSummary summary = new FireSituationMarkerSummary();
        summary.setItems(markers.stream().map(this::toMarkerItem).collect(Collectors.toList()));
        return summary;
    }

    private FireSituationMarkerItem toMarkerItem(FacFireSituationMarker marker) {
        FireSituationMarkerItem item = new FireSituationMarkerItem();
        item.setId(marker.getMarkerCode());
        item.setKind(marker.getMarkerKind());
        item.setTitle(marker.getTitle());
        item.setSubtitle(marker.getSubtitle());
        item.setLongitude(marker.getLongitude());
        item.setLatitude(marker.getLatitude());
        item.setImportant(marker.getImportantFlag());
        item.setIconUrl(marker.getIconUrl());
        item.setLevel(marker.getLevelName());
        item.setTargetId(marker.getTargetId());
        return item;
    }
}
