package com.sinopec.mmsecurity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinopec.mmsecurity.entity.SysDictType;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SysDictTypeMapper extends BaseMapper<SysDictType> {

    /**
     * 统计同 dict_code 的字典类型数——<b>包含逻辑删除行</b>（唯一索引与逻辑删除的语义鸿沟，见
     * {@link SysUserMapper#countUsernameIncludingDeleted}）。{@code excludeId} 用于更新场景排除自身。
     */
    @Select("SELECT COUNT(*) FROM sys_dict_type WHERE dict_code = #{dictCode} AND id <> #{excludeId}")
    long countDictCodeIncludingDeleted(@Param("dictCode") String dictCode, @Param("excludeId") long excludeId);
}
