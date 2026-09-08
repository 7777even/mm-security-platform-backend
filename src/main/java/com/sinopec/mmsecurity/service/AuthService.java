package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.common.cache.IdNameCacheService;
import com.sinopec.mmsecurity.dto.LoginRequest;
import com.sinopec.mmsecurity.dto.MenuVO;
import com.sinopec.mmsecurity.dto.TokenResponse;
import com.sinopec.mmsecurity.entity.SysMenu;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import com.sinopec.mmsecurity.security.JwtUtil;
import com.sinopec.mmsecurity.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder;
    private final IdNameCacheService idNameCache;

    @Value("${jwt.access-ttl}")
    private long accessTtl;

    /**
     * 顶部导航栏菜单（驱动前端顶部 Tab）。从 sys_menu 表按当前登录角色过滤加载
     * （RBAC）：只返回 allowed_roles 含当前角色、且为顶层主模块 fm-* 的菜单。
     * id（=sys_menu.code）必须与前端 {@code MENU_ROUTE_SPECS} 中对应 key 对齐，否则前端
     * buildDynamicRoutes 会跳过该项、降级为内置 DEFAULT_MENUS。
     * 其余子应用（fm-rescue / fm-typhoon / fm-production-area / fm-major-hazard /
     * fm-communication / fm-video-control / fm-video-wall）不进顶部导航，由前端
     * SECONDARY_ROUTES 二级隐藏路由承载（WujieHost 挂载对应子应用），故不在此返回。
     */

    /** 首次启动且 sys_user 为空时，写入默认 admin（用户名 admin / 密码 admin@2026） */
    @PostConstruct
    public void ensureAdmin() {
        Long cnt = userMapper.selectCount(null);
        if (cnt != null && cnt == 0) {
            SysUser u = new SysUser();
            u.setUsername("admin");
            u.setPasswordHash(encoder.encode("admin@2026"));
            u.setRealName("系统管理员");
            u.setRole("ADMIN");
            u.setStatus(1);
            u.setDeleted(0);
            u.setCreatedAt(LocalDateTime.now());
            u.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(u);
            log.info("已写入默认管理员账号 admin / admin@2026");
        }
    }

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
                jwtUtil.issueAccess(u.getUsername(), u.getRole()),
                accessTtl);
    }

    public TokenResponse refresh(String refreshToken) {
        var claims = jwtUtil.parse(refreshToken);
        if (claims == null || !"refresh".equals(claims.get("type", String.class))) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "refresh 令牌无效");
        }
        return TokenResponse.of(
                jwtUtil.issueAccess(claims.getSubject(), "ADMIN"),
                accessTtl);
    }

    /** 签发刷新令牌：仅由 AuthController 经 HttpOnly Cookie 下发，绝不进响应 body。 */
    public String issueRefreshToken(String username) {
        return jwtUtil.issueRefresh(username);
    }

    public Map<String, Object> me() {
        String username = UserContext.username();
        SysUser u = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        String realName = (u != null && u.getRealName() != null)
                ? u.getRealName()
                : (username == null ? "" : username);
        String role = (u != null && u.getRole() != null)
                ? u.getRole()
                : (UserContext.role() == null ? "" : UserContext.role());
        return Map.of(
                "username", username == null ? "" : username,
                "realName", realName,
                "role", role);
    }

    public List<MenuVO> menus() {
        String ctxRole = UserContext.role();
        final String role = (ctxRole == null) ? "ANONYMOUS" : ctxRole;
        List<SysMenu> all = idNameCache.allMenus();
        return all.stream()
                .filter(m -> roleAllowed(m.getAllowedRoles(), role))
                .sorted(Comparator.comparingInt(m -> m.getSort() == null ? 0 : m.getSort()))
                .map(m -> new MenuVO(m.getMenuKey(), m.getName(), m.getPath()))
                .toList();
    }

    /** RBAC 判定：菜单 allowed_roles（逗号分隔）是否包含当前角色 */
    private boolean roleAllowed(String allowedRoles, String role) {
        if (allowedRoles == null || allowedRoles.isBlank()) {
            return false;
        }
        for (String r : allowedRoles.split(",")) {
            if (r.trim().equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }
}
