package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.MsdsDetail;
import com.sinopec.mmsecurity.dto.MsdsItem;
import com.sinopec.mmsecurity.dto.MsdsList;
import com.sinopec.mmsecurity.entity.FacMsds;
import com.sinopec.mmsecurity.mapper.FacMsdsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 化学品 MSDS 域服务（移动端化学品知识 / MSDS 详情）。
 *
 * <p>数据来源为 fac_msds 真实表，取代 apps/mobile/data/mock.ts 的 msds 静态数据。
 * 纯只读：列表 + 按 CAS 号详情。
 */
@Service
@RequiredArgsConstructor
public class MsdsService {

    private final FacMsdsMapper msdsMapper;

    /** 化学品 MSDS 列表（按 id 升序）。 */
    public MsdsList list() {
        List<MsdsItem> items = msdsMapper.selectList(
                        new LambdaQueryWrapper<FacMsds>().orderByAsc(FacMsds::getId))
                .stream()
                .map(MsdsService::toItem)
                .toList();
        MsdsList result = new MsdsList();
        result.setItems(items);
        result.setTotal(items.size());
        return result;
    }

    /** 化学品 MSDS 详情（按 CAS 号）；未命中抛 NOT_FOUND。 */
    public MsdsDetail detail(String cas) {
        FacMsds row = msdsMapper.selectList(
                        new LambdaQueryWrapper<FacMsds>()
                                .eq(FacMsds::getCas, cas)
                                .orderByAsc(FacMsds::getId))
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "MSDS 不存在：cas=" + cas));
        return toDetail(row);
    }

    private static MsdsItem toItem(FacMsds r) {
        MsdsItem item = new MsdsItem();
        item.setId(r.getId());
        item.setName(r.getName());
        item.setCas(r.getCas());
        item.setClassification(r.getClassification());
        return item;
    }

    private static MsdsDetail toDetail(FacMsds r) {
        MsdsDetail d = new MsdsDetail();
        d.setId(r.getId());
        d.setName(r.getName());
        d.setCas(r.getCas());
        d.setClassification(r.getClassification());
        d.setState(r.getState());
        d.setBoilingPoint(r.getBoilingPoint());
        d.setFlashPoint(r.getFlashPoint());
        d.setExplosionLimit(r.getExplosionLimit());
        d.setStorage(r.getStorage());
        d.setSafety(r.getSafety());
        d.setEmergency(r.getEmergency());
        return d;
    }
}
