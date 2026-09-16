package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.CommunicationRecord;
import com.sinopec.mmsecurity.dto.CommunicationRecordList;
import com.sinopec.mmsecurity.entity.FacCommRecord;
import com.sinopec.mmsecurity.mapper.FacCommRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通讯通知记录服务（短信 / 电话通话 / 广播播报 / APP推送 / 语音对讲）。
 *
 * <p>数据来源为 V57 落地的 fac_comm_record 真实表，按 record_type 过滤。
 * type 缺省返回全部类型，便于前端一次拿全后再按页签拆分。
 */
@Service
@RequiredArgsConstructor
public class CommRecordService {

    private final FacCommRecordMapper commRecordMapper;

    /** 按记录类型查询通知记录；type 为空返回全部。 */
    public CommunicationRecordList list(String type) {
        LambdaQueryWrapper<FacCommRecord> query = new LambdaQueryWrapper<>();
        if (type != null && !type.isBlank()) {
            query.eq(FacCommRecord::getRecordType, type);
        }
        query.orderByAsc(FacCommRecord::getRecordType, FacCommRecord::getId);
        List<FacCommRecord> rows = commRecordMapper.selectList(query);
        CommunicationRecordList result = new CommunicationRecordList();
        result.setItems(rows.stream().map(this::toItem).collect(Collectors.toList()));
        result.setTotal(rows.size());
        return result;
    }

    private CommunicationRecord toItem(FacCommRecord entity) {
        CommunicationRecord item = new CommunicationRecord();
        item.setRecordNo(entity.getRecordNo());
        item.setRecordType(entity.getRecordType());
        item.setOccurredAt(entity.getOccurredAt());
        item.setCategory(entity.getCategory());
        item.setSender(entity.getSender());
        item.setReceiver(entity.getReceiver());
        item.setSummary(entity.getSummary());
        item.setResult(entity.getResult());
        item.setDuration(entity.getDuration());
        item.setChannel(entity.getChannel());
        item.setDirection(entity.getDirection());
        item.setContentType(entity.getContentType());
        return item;
    }
}
