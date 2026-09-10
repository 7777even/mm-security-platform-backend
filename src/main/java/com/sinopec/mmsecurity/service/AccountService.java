package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.common.cache.IdNameCacheService;
import com.sinopec.mmsecurity.common.cache.PasswordStateCache;
import com.sinopec.mmsecurity.dto.MeResult;
import com.sinopec.mmsecurity.dto.PasswordChangeRequest;
import com.sinopec.mmsecurity.dto.ProfileUpdateRequest;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import com.sinopec.mmsecurity.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 个人中心服务：本人改密、本人资料维护。
 *
 * <p>越权边界：本服务只操作 {@code UserContext} 当前登录用户，**不接受目标用户参数**，
 * 因此不存在水平越权的入口；角色 / 状态不可自改（防自我提权）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder encoder;
    private final PasswordPolicy passwordPolicy;
    private final IdNameCacheService idNameCache;
    private final SystemAuditHelper audit;
    private final AuthService authService;
    private final PasswordStateCache passwordStateCache;

    /** 本人改密：校验旧口令 → 策略校验 → 落新哈希 → 清强制改密标记。 */
    public void changePassword(PasswordChangeRequest req) {
        SysUser u = requireCurrentUser();
        if (!encoder.matches(req.getOldPassword(), u.getPasswordHash())) {
            // 不区分「原密码错误」与其它，统一提示；错误响应不泄露哈希相关信息。
            throw new BusinessException(ResultCode.PARAM_INVALID, "原密码不正确");
        }
        passwordPolicy.validate(u.getUsername(), req.getNewPassword(), req.getOldPassword());

        u.setPasswordHash(encoder.encode(req.getNewPassword()));
        u.setPwdUpdatedAt(LocalDateTime.now());
        u.setMustChangePwd(0);
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);

        passwordStateCache.evict(u.getUsername());
        audit.record("system.account.change-password", Map.of("username", u.getUsername()));
    }

    /** 本人资料修改：仅姓名；返回最新 me（含权限码）。 */
    public MeResult updateProfile(ProfileUpdateRequest req) {
        SysUser u = requireCurrentUser();
        u.setRealName(req.getRealName());
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);

        // 引用数据缓存失效：否则列表 id→姓名 解析会「改了但不变」（id-name-cache.md §3 强制要求）
        idNameCache.evictUser(u.getId());
        audit.record("system.account.update-profile", Map.of("username", u.getUsername()));
        return authService.me();
    }

    private SysUser requireCurrentUser() {
        String username = UserContext.username();
        SysUser u = username == null ? null : userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (u == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录态无效，请重新登录");
        }
        return u;
    }
}
