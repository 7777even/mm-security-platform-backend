package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.common.cache.IdNameCacheService;
import com.sinopec.mmsecurity.dto.LoginRequest;
import com.sinopec.mmsecurity.dto.MeResult;
import com.sinopec.mmsecurity.dto.MenuVO;
import com.sinopec.mmsecurity.dto.TokenResponse;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import com.sinopec.mmsecurity.security.JwtUtil;
import com.sinopec.mmsecurity.security.RoleAuthorityService;
import com.sinopec.mmsecurity.security.TokenVersionService;
import com.sinopec.mmsecurity.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 认证域服务：登录、续期、当前用户、菜单。
 *
 * <p>RBAC 口径（V32 起）：菜单与权限码由 {@code sys_role_menu} 授权，
 * 经 {@link RoleAuthorityService} 解析（缓存 + 写时失效）。
 * {@code sys_menu.allowed_roles} 已降级为只读兼容列，不再参与判定。</p>
 *
 * <p>启动期种子（默认管理员 / 角色 / 兜底授权）已迁至
 * {@link RbacBootstrapService}，本服务只承担运行期认证职责。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder;
    private final IdNameCacheService idNameCache;
    private final RoleAuthorityService roleAuthorityService;
    private final TokenVersionService tokenVersionService;

    @Value("${jwt.access-ttl}")
    private long accessTtl;

    public TokenResponse login(LoginRequest req) {
        SysUser u = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername()));
        if (u == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (u.getStatus() != null && u.getStatus() != 1) {
            throw new BusinessException(401, "账号已被禁用");
        }
        if (!encoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        return TokenResponse.of(
                jwtUtil.issueAccess(u.getUsername(), u.getRole(), tokenVersionService.current(u.getUsername())),
                accessTtl);
    }

    /**
     * 续期：换发 access 令牌。
     *
     * <p><b>安全修复</b>：早期实现将新令牌角色硬编码为 {@code "ADMIN"}（潜伏提权缺陷），
     * 现改为回查库中真实角色；账号不存在或已禁用则拒绝续期，强制重新登录，
     * 避免「已停用账号凭 refresh Cookie 无限续命」。</p>
     */
    public TokenResponse refresh(String refreshToken) {
        var claims = jwtUtil.parse(refreshToken);
        if (claims == null || !"refresh".equals(claims.get("type", String.class))) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "refresh 令牌无效");
        }
        String username = claims.getSubject();
        SysUser u = username == null ? null : userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (u == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "账号不存在，请重新登录");
        }
        if (u.getStatus() != null && u.getStatus() != 1) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "账号已被禁用");
        }
        return TokenResponse.of(
                jwtUtil.issueAccess(u.getUsername(), u.getRole(), tokenVersionService.current(u.getUsername())),
                accessTtl);
    }

    /** 签发刷新令牌：仅由 AuthController 经 HttpOnly Cookie 下发，绝不进响应 body。 */
    public String issueRefreshToken(String username) {
        return jwtUtil.issueRefresh(username);
    }

    /** 当前用户身份 + 权限码全集（前端权限判定唯一来源）。 */
    public MeResult me() {
        String username = UserContext.username();
        SysUser u = username == null ? null : userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        String ctxRole = UserContext.role();
        String role = (u != null && u.getRole() != null && !u.getRole().isBlank())
                ? u.getRole()
                : (ctxRole == null ? "" : ctxRole);

        MeResult r = new MeResult();
        r.setUsername(username == null ? "" : username);
        r.setRealName(u != null && u.getRealName() != null ? u.getRealName() : r.getUsername());
        r.setRole(role);
        r.setRoles(role.isEmpty() ? List.of() : List.of(role));

        List<String> perms = new ArrayList<>(roleAuthorityService.permsOf(role));
        Collections.sort(perms);
        r.setPerms(perms);

        r.setMustChangePwd(u != null && u.getMustChangePwd() != null && u.getMustChangePwd() == 1);
        return r;
    }

    /**
     * 顶部导航栏菜单（驱动前端顶部 Tab）。仅返回当前角色经 sys_role_menu 授权、
     * 且为顶层主模块 {@code fm-*} 目录的菜单。
     *
     * <p>id（=sys_menu.code）必须与前端 {@code MENU_ROUTE_SPECS} 中对应 key 对齐，否则前端
     * buildDynamicRoutes 会跳过该项、降级为内置 DEFAULT_MENUS。
     * system 等管理域节点不进顶部导航，由前端 SECONDARY_ROUTES 二级隐藏路由承载。</p>
     */
    public List<MenuVO> menus() {
        String ctxRole = UserContext.role();
        if (ctxRole == null || ctxRole.isBlank()) {
            return List.of();
        }
        Set<Long> allowedIds = roleAuthorityService.menuIdsOf(ctxRole);
        if (allowedIds.isEmpty()) {
            return List.of();
        }
        return idNameCache.allMenus().stream()
                .filter(m -> m.getId() != null && allowedIds.contains(m.getId()))
                .filter(m -> m.getMenuKey() != null && m.getMenuKey().startsWith("fm-"))
                // BUTTON 节点不参与导航装配
                .filter(m -> !"BUTTON".equalsIgnoreCase(m.getMenuType()))
                .filter(m -> m.getStatus() == null || m.getStatus() == 1)
                .filter(m -> m.getVisible() == null || m.getVisible() == 1)
                .sorted(Comparator.comparingInt(m -> m.getSort() == null ? 0 : m.getSort()))
                .map(m -> new MenuVO(m.getMenuKey(), m.getName(), m.getPath()))
                .toList();
    }
}
