package com.sinopec.mmsecurity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinopec.mmsecurity.entity.SysRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 统计同 role_code 的角色数——<b>包含逻辑删除行</b>（唯一索引与逻辑删除的语义鸿沟，见
     * {@link SysUserMapper#countUsernameIncludingDeleted}）。{@code excludeId} 用于更新场景排除自身。
     */
    @Select("SELECT COUNT(*) FROM sys_role WHERE role_code = #{roleCode} AND id <> #{excludeId}")
    long countRoleCodeIncludingDeleted(@Param("roleCode") String roleCode, @Param("excludeId") long excludeId);
}
