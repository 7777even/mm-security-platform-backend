package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.common.cache.IdNameCacheService;
import com.sinopec.mmsecurity.common.cache.PasswordStateCache;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.PasswordResetResult;
import com.sinopec.mmsecurity.dto.SystemUserCreate;
import com.sinopec.mmsecurity.dto.SystemUserItem;
import com.sinopec.mmsecurity.dto.SystemUserPageResult;
import com.sinopec.mmsecurity.dto.SystemUserUpdate;
import com.sinopec.mmsecurity.entity.SysRole;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysRoleMapper;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import com.sinopec.mmsecurity.security.DataScopeResolver;
import com.sinopec.mmsecurity.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 系统用户管理服务。
 *
 * <p><b>三条硬防护（服务端强制，design §7.2）</b>：</p>
 * <ol>
 *   <li><b>禁操作自己</b>：删除 / 停用 / 改角色对自己一律 403（防自锁与自我提权）；</li>
 *   <li><b>保护最后一个启用 ADMIN</b>：会使启用 ADMIN 归零的删 / 停 / 改角色请求返回 409（防全员锁死）；</li>
 *   <li><b>角色必须存在且启用</b>：分配不存在或已停用的角色返回 100。</li>
 * </ol>
 *
 * <p>所有写操作落服务端审计（{@link SystemAuditHelper}）；口令一律 BCrypt，
 * 任何响应体与审计 detail 都不含口令明文或哈希。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemUserService {

    private static final String ADMIN_ROLE = "ADMIN";

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final BCryptPasswordEncoder encoder;
    private final PasswordPolicy passwordPolicy;
    private final IdNameCacheService idNameCache;
    private final PasswordStateCache passwordStateCache;
    private final SystemAuditHelper audit;
    private final DataScopeResolver dataScopeResolver;

    /** 分页查询（keyword 模糊匹配用户名 / 姓名）。 */
    public SystemUserPageResult page(long page, long size, String keyword, Integer status, String roleCode) {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.trim();
            qw.and(w -> w.like(SysUser::getUsername, k).or().like(SysUser::getRealName, k));
        }
        if (status != null) {
            qw.eq(SysUser::getStatus, status);
        }
        if (roleCode != null && !roleCode.isBlank()) {
            qw.eq(SysUser::getRole, roleCode.trim().toUpperCase(Locale.ROOT));
        }
        qw.orderByDesc(SysUser::getId);

        Page<SysUser> p = userMapper.selectPage(new Page<>(page, size), qw);
        Map<String, String> roleNames = roleNameMap();

        SystemUserPageResult r = new SystemUserPageResult();
        r.setList(p.getRecords().stream().map(u -> toItem(u, roleNames)).toList());
        r.setTotal(p.getTotal());
        r.setPage(p.getCurrent());
        r.setSize(p.getSize());
        return r;
    }

    /** 用户详情（不存在 → 404）。 */
    public SystemUserItem get(Long id) {
        return toItem(require(id), roleNameMap());
    }

    /** 新增用户：用户名唯一校验 + 角色存在性校验 + 初始口令策略校验；置强制首登改密。 */
    @Transactional
    public SystemUserItem create(SystemUserCreate req) {
        String username = req.getUsername().trim();
        // 唯一性判定必须**含逻辑删除行**：username 有唯一索引而删除是逻辑删除，
        // 用 selectCount（自动过滤 deleted=0）会漏判已删账号，导致「预检查放行 → DB 唯一键拒绝」
        // 只抛出笼统的数据冲突。见 SysUserMapper#countUsernameIncludingDeleted。
        if (userMapper.countUsernameIncludingDeleted(username) > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名已存在（用户名不可复用，含历史已删除账号）");
        }
        SysRole role = requireEnabledRole(req.getRoleCode());
        passwordPolicy.validate(username, req.getPassword(), null);

        SysUser u = new SysUser();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(req.getPassword()));
        u.setRealName(req.getRealName());
        u.setRole(role.getRoleCode());
        u.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        u.setMustChangePwd(1);
        u.setZoneCodes(req.getZoneCodes());
        LocalDateTime now = LocalDateTime.now();
        u.setPwdUpdatedAt(now);
        u.setCreatedAt(now);
        u.setUpdatedAt(now);
        u.setDeleted(0);
        userMapper.insert(u);

        dataScopeResolver.invalidateUser(username);
        audit.record("system.user.create", Map.of("username", username, "roleCode", role.getRoleCode()));
        return toItem(u, roleNameMap());
    }

    /** 修改用户：姓名 / 角色 / 状态；涉及角色或状态变更时执行硬防护。 */
    @Transactional
    public SystemUserItem update(Long id, SystemUserUpdate req) {
        SysUser target = require(id);
        boolean roleChanged = req.getRoleCode() != null && !req.getRoleCode().isBlank()
                && !req.getRoleCode().trim().equalsIgnoreCase(target.getRole());
        boolean statusChanged = req.getStatus() != null && !req.getStatus().equals(target.getStatus());
        boolean zoneChanged = req.getZoneCodes() != null && !java.util.Objects.equals(req.getZoneCodes(), target.getZoneCodes());

        if (roleChanged) {
            assertNotSelf(target, "不可修改自己的角色");
            assertNotLastEnabledAdmin(target);
            target.setRole(requireEnabledRole(req.getRoleCode()).getRoleCode());
        }
        if (statusChanged && req.getStatus() != null && req.getStatus() != 1) {
            assertNotSelf(target, "不可停用自己的账号");
            assertNotLastEnabledAdmin(target);
        }
        if (req.getRealName() != null) {
            target.setRealName(req.getRealName());
        }
        if (req.getStatus() != null) {
            target.setStatus(req.getStatus());
        }
        if (req.getZoneCodes() != null) {
            target.setZoneCodes(req.getZoneCodes());
        }
        target.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(target);

        if (zoneChanged) {
            dataScopeResolver.invalidateUser(target.getUsername());
        }
        idNameCache.evictUser(target.getId());
        passwordStateCache.evict(target.getUsername());
        audit.record("system.user.update", Map.of(
                "username", target.getUsername(),
                "roleChanged", roleChanged,
                "status", target.getStatus() == null ? -1 : target.getStatus(),
                "zoneChanged", zoneChanged));
        return toItem(target, roleNameMap());
    }

    /** 逻辑删除用户：禁删自己、禁删最后一个启用 ADMIN。 */
    @Transactional
    public DeleteResult delete(Long id) {
        SysUser target = require(id);
        assertNotSelf(target, "不可删除自己的账号");
        assertNotLastEnabledAdmin(target);
        userMapper.deleteById(target.getId());

        idNameCache.evictUser(target.getId());
        passwordStateCache.evict(target.getUsername());
        audit.record("system.user.delete", Map.of("username", target.getUsername()));

        DeleteResult r = new DeleteResult();
        r.setOk(true);
        return r;
    }

    /** 启用 / 停用（停用走硬防护；停用后现有 refresh Cookie 亦无法续期）。 */
    @Transactional
    public SystemUserItem updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "status 只能为 0（停用）或 1（启用）");
        }
        return update(id, buildStatusUpdate(status));
    }

    /** 单独分配角色（等价于 update 的角色分支，独立端点便于前端按权限码粒度控制）。 */
    @Transactional
    public SystemUserItem assignRole(Long id, String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "角色不能为空");
        }
        SystemUserUpdate req = new SystemUserUpdate();
        req.setRoleCode(roleCode);
        return update(id, req);
    }

    /** 管理员重置口令：随机临时口令 + 强制下次登录改密；响应一次性返回临时口令。 */
    @Transactional
    public PasswordResetResult resetPassword(Long id) {
        SysUser target = require(id);
        String temp = passwordPolicy.generate();
        target.setPasswordHash(encoder.encode(temp));
        target.setMustChangePwd(1);
        target.setPwdUpdatedAt(LocalDateTime.now());
        target.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(target);

        passwordStateCache.evict(target.getUsername());
        // 审计只记事实，绝不记临时口令
        audit.record("system.user.reset-password", Map.of("username", target.getUsername()));

        PasswordResetResult r = new PasswordResetResult();
        r.setTemporaryPassword(temp);
        r.setMustChangePwd(true);
        return r;
    }

    // ---------------------------------------------------------------- 内部工具

    private SystemUserUpdate buildStatusUpdate(Integer status) {
        SystemUserUpdate req = new SystemUserUpdate();
        req.setStatus(status);
        return req;
    }

    private SysUser require(Long id) {
        SysUser u = userMapper.selectById(id);
        if (u == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return u;
    }

    private SysRole requireEnabledRole(String roleCode) {
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode == null ? "" : roleCode.trim().toUpperCase(Locale.ROOT)));
        if (role == null || role.getStatus() == null || role.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "角色不存在或已停用：" + roleCode);
        }
        return role;
    }

    /** 硬防护 1：禁止对当前登录用户自身执行破坏性操作。 */
    private void assertNotSelf(SysUser target, String message) {
        String current = UserContext.username();
        if (current != null && current.equals(target.getUsername())) {
            throw new BusinessException(ResultCode.FORBIDDEN, message);
        }
    }

    /** 硬防护 2：禁止使启用状态 ADMIN 归零。 */
    private void assertNotLastEnabledAdmin(SysUser target) {
        if (target.getRole() == null || !ADMIN_ROLE.equalsIgnoreCase(target.getRole())) {
            return;
        }
        if (target.getStatus() == null || target.getStatus() != 1) {
            return;
        }
        Long enabled = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, ADMIN_ROLE)
                .eq(SysUser::getStatus, 1));
        if (enabled != null && enabled <= 1) {
            throw new BusinessException(ResultCode.CONFLICT, "必须保留至少一个启用状态的管理员");
        }
    }

    private Map<String, String> roleNameMap() {
        Map<String, String> map = new HashMap<>();
        List<SysRole> roles = roleMapper.selectList(null);
        for (SysRole r : roles) {
            if (r.getRoleCode() != null) {
                map.put(r.getRoleCode().toUpperCase(Locale.ROOT), r.getRoleName());
            }
        }
        return map;
    }

    private SystemUserItem toItem(SysUser u, Map<String, String> roleNames) {
        SystemUserItem item = new SystemUserItem();
        item.setId(u.getId());
        item.setUsername(u.getUsername());
        item.setRealName(u.getRealName());
        item.setRoleCode(u.getRole());
        item.setRoleName(u.getRole() == null ? null
                : roleNames.getOrDefault(u.getRole().toUpperCase(Locale.ROOT), u.getRole()));
        item.setStatus(u.getStatus());
        item.setZoneCodes(u.getZoneCodes());
        item.setMustChangePwd(u.getMustChangePwd() != null && u.getMustChangePwd() == 1);
        item.setCreatedAt(u.getCreatedAt());
        item.setUpdatedAt(u.getUpdatedAt());
        return item;
    }
}
