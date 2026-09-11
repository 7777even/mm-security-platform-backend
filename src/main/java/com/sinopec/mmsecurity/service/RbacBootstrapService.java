package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.entity.SysMenu;
import com.sinopec.mmsecurity.entity.SysRole;
import com.sinopec.mmsecurity.entity.SysRoleMenu;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysMenuMapper;
import com.sinopec.mmsecurity.mapper.SysRoleMapper;
import com.sinopec.mmsecurity.mapper.SysRoleMenuMapper;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import com.sinopec.mmsecurity.security.RoleAuthorityService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RBAC 启动期引导（幂等）：角色种子、默认管理员账号、内置角色兜底授权。
 *
 * <p>职责从原 {@code AuthService.ensureAdmin()} 拆出并扩展——启动期「确保系统可用」的
 * 关注点（账号 + 角色 + 授权）集中在此，业务服务不再承担建种子职责。</p>
 *
 * <p><b>幂等与克制原则</b>：</p>
 * <ul>
 *   <li>角色：按 role_code 逐个确保存在（已存在即跳过，不覆盖运维改名/停用）。</li>
 *   <li>默认管理员：仅当 {@code sys_user} 表为空时写入（保持原语义，不在运行时反复重置）；
 *       写入时置 {@code must_change_pwd=1}，强制首登改密（默认口令不得当生产凭证）。</li>
 *   <li>内置角色授权：仅当 ADMIN 角色**一条授权都没有**时补齐全量，作为锁死兜底；
 *       已有授权则不动，避免把运维有意收回的授权重新点亮。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RbacBootstrapService {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String DEFAULT_ADMIN_USER = "admin";
    private static final String DEFAULT_ADMIN_PWD = "admin@2026";

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final BCryptPasswordEncoder encoder;
    private final RoleAuthorityService roleAuthorityService;

    /**
     * 种子默认管理员是否置 must_change_pwd=1。生产必须 true（admin@2026 是已知弱口令）；
     * dev 置 false（内存库每次重启重建，强制改密会反复阻断联调）。
     */
    @Value("${app.password.force-change-default-admin:true}")
    private boolean forceChangeDefaultAdmin;

    private record RoleSeed(String code, String name, String description, String dataScope, int builtIn, int sort) {}

    private static final List<RoleSeed> ROLE_SEEDS = List.of(
            new RoleSeed("ADMIN", "系统管理员", "内置超级管理员，拥有全部菜单与权限", "ALL", 1, 0),
            new RoleSeed("COMMANDER", "总指挥", "园区应急总指挥", "ALL", 0, 10),
            new RoleSeed("SCHEDULER", "值班调度", "值班调度岗", "DEPT", 0, 20),
            new RoleSeed("TEAM_LEADER", "属地班长", "属地运行部班长", "DEPT", 0, 30),
            new RoleSeed("INNER_OPER", "内操", "装置内操", "SELF", 0, 40),
            new RoleSeed("OUTER_OPER", "外操", "装置外操", "SELF", 0, 50));

    @PostConstruct
    public void bootstrap() {
        ensureRoles();
        ensureAdminUser();
        ensureAdminGrants();
        roleAuthorityService.reloadRolePerms();
    }

    /** 角色种子幂等落库（已存在则不覆盖）。 */
    private void ensureRoles() {
        for (RoleSeed seed : ROLE_SEEDS) {
            Long exists = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getRoleCode, seed.code()));
            if (exists != null && exists > 0) {
                continue;
            }
            SysRole r = new SysRole();
            r.setRoleCode(seed.code());
            r.setRoleName(seed.name());
            r.setDescription(seed.description());
            r.setDataScope(seed.dataScope());
            r.setStatus(1);
            r.setBuiltIn(seed.builtIn());
            r.setSortOrder(seed.sort());
            r.setDeleted(0);
            LocalDateTime now = LocalDateTime.now();
            r.setCreatedAt(now);
            r.setUpdatedAt(now);
            roleMapper.insert(r);
            log.info("已补写角色种子 {}", seed.code());
        }
    }

    /** 默认管理员：仅当 sys_user 为空时写入；角色固定 ADMIN，强制首登改密。 */
    private void ensureAdminUser() {
        Long cnt = userMapper.selectCount(null);
        if (cnt != null && cnt > 0) {
            return;
        }
        SysUser u = new SysUser();
        u.setUsername(DEFAULT_ADMIN_USER);
        u.setPasswordHash(encoder.encode(DEFAULT_ADMIN_PWD));
        u.setRealName("系统管理员");
        u.setRole(ADMIN_ROLE);
        u.setStatus(1);
        u.setMustChangePwd(forceChangeDefaultAdmin ? 1 : 0);
        u.setPwdUpdatedAt(LocalDateTime.now());
        u.setDeleted(0);
        LocalDateTime now = LocalDateTime.now();
        u.setCreatedAt(now);
        u.setUpdatedAt(now);
        userMapper.insert(u);
        // 安全：初始口令绝不写入日志（明文口令落日志等同泄露，且日志常被引流到集中平台）。
        // DEFAULT_ADMIN_PWD 保留为固定常量是 dev 联调契约（联调账号见 docs/system-facts.md），
        // 生产由 app.password.force-change-default-admin=true 强制首登改密兜底。
        log.info("已写入默认管理员账号 {}（首次登录须改密，初始口令不在日志中输出）", DEFAULT_ADMIN_USER);
    }

    /** ADMIN 兜底授权：仅当其一条授权都没有时补齐全部启用节点（防锁死）。 */
    private void ensureAdminGrants() {
        SysRole admin = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, ADMIN_ROLE));
        if (admin == null) {
            return;
        }
        Long granted = roleMenuMapper.selectCount(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, admin.getId()));
        if (granted != null && granted > 0) {
            return;
        }
        List<SysMenu> menus = menuMapper.selectList(null);
        for (SysMenu m : menus) {
            if (m.getStatus() != null && m.getStatus() != 1) {
                continue;
            }
            SysRoleMenu link = new SysRoleMenu();
            link.setRoleId(admin.getId());
            link.setMenuId(m.getId());
            link.setCreatedAt(LocalDateTime.now());
            roleMenuMapper.insert(link);
        }
        log.warn("ADMIN 角色无任何授权，已补齐全部启用菜单节点（{} 条）作为锁死兜底", menus.size());
    }
}
