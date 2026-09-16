package com.sinopec.mmsecurity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinopec.mmsecurity.entity.FacCommRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通讯通知记录 Mapper（表 fac_comm_record）。
 *
 * <p>只读域：仅用 MyBatis-Plus BaseMapper 的 selectList，无自定义 SQL。
 */
@Mapper
public interface FacCommRecordMapper extends BaseMapper<FacCommRecord> {
}
