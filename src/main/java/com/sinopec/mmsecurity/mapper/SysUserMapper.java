package com.sinopec.mmsecurity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinopec.mmsecurity.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 统计同名用户数——<b>包含逻辑删除行</b>。
     *
     * <p>为什么需要它：{@code sys_user.username} 有唯一索引，而删除是**逻辑删除**（deleted=1），
     * 行仍物理存在。MyBatis-Plus 的服务层查询会自动追加 {@code deleted=0}，因此
     * {@code selectCount} 看不到已删账号 → 预检查放行、DB 唯一键拒绝，
     * 只能抛出笼统的「数据冲突」，且语义含混。</p>
     *
     * <p>本方法用原生 SQL 绕过逻辑删除过滤，使「用户名不唯一」的判定与数据库约束一致，
     * 从而给出准确提示。用户名**不回收**是有意设计：回收会让新旧账号在审计上无法区分。</p>
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username}")
    long countUsernameIncludingDeleted(@Param("username") String username);
}
