package com.sinopec.mmsecurity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinopec.mmsecurity.entity.FacAlarm;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AlarmMapper extends BaseMapper<FacAlarm> {

    /**
     * 取当年最大告警序号后缀（AE-{year}-NNN 的 NNN）。
     * 用 {@code ||} 拼接 + LIKE，跨 h2 / MySQL / PostgreSQL / 达梦(Oracle 系) 均安全；
     * 避免原 for 循环全表物化求 max（告警表只增，量大会线性变慢）。
     *
     * @return 形如 {@code AE-2026-007} 的最大告警号，无匹配时返回 null
     */
    @Select("SELECT MAX(alarm_id) FROM fac_alarm WHERE deleted = 0 AND alarm_id LIKE 'AE-' || #{year} || '-%'")
    String selectMaxAlarmIdForYear(@Param("year") int year);
}
