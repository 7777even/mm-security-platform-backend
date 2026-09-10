package com.sinopec.mmsecurity.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * data_scope 行级 ABAC 解析器：按「角色 data_scope + 用户 zone_codes」解析当前登录用户
 * 可访问的防区集合。
 *
 * <p><b>解析语义</b>（与 design.md §2 一致）：</p>
 * <ul>
 *   <li>{@code data_scope = ALL} → 返回 {@code null}（调用方不加 WHERE，看全部）；</li>
 *   <li>否则 → 返回用户的 {@code zone_codes} 解析集合；空集表示无任何可见防区（最小权限）；</li>
 *   <li>匿名（端点公开场景，理论上 JwtFilter 已拦截）→ 返回 {@code null}（不过滤，保持公开行为）。</li>
 * </ul>
 *
 * <p><b>缓存</b>：用户防区集合走 Caffeine 读穿缓存（TTL 5min + 用户 zone_codes 变更时
 * {@link #invalidateUser(String)} 主动失效），用户维度变更立即生效。</p>
 *
 * <p><b>不写令牌</b>：遵循 ADR-3，data_scope / zone 由服务端经本解析器 + 用户缓存解析，
 * 令牌仍只携 role。角色 data_scope 变更复用 {@link RoleAuthorityService#reloadRolePerms()} 失效。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataScopeResolver {

    private final RoleAuthorityService roleAuthorityService;
    private final SysUserMapper userMapper;

    private Cache<String, Set<String>> userZoneCache;

    @PostConstruct
    void init() {
        this.userZoneCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(1024)
                .build();
    }

    /**
     * 解析当前登录用户可访问的防区集合。
     *
     * @return {@code null} = 不过滤（data_scope=ALL 或匿名）；空集 = 无任何可见防区；非空 = 允许防区集合
     */
    public Set<String> resolveZones() {
        LoginUser user = UserContext.get();
        if (user == null) {
            // 匿名（端点公开场景）：无法判定范围 → 不过滤，保持公开行为。
            return null;
        }
        String scope = roleAuthorityService.dataScopeOf(user.getRole());
        if ("ALL".equalsIgnoreCase(scope)) {
            return null;
        }
        return userZoneCache.get(user.getUsername(), this::loadUserZones);
    }

    /** 用户 zone_codes 变更后调用，立即失效其缓存（最小权限变更立即生效）。 */
    public void invalidateUser(String username) {
        if (userZoneCache != null && username != null) {
            userZoneCache.invalidate(username);
        }
    }

    private Set<String> loadUserZones(String username) {
        SysUser u = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (u == null || u.getZoneCodes() == null || u.getZoneCodes().isBlank()) {
            return Set.of();
        }
        Set<String> zones = new LinkedHashSet<>();
        for (String z : u.getZoneCodes().split(",")) {
            String t = z.trim();
            if (!t.isEmpty()) {
                zones.add(t);
            }
        }
        return zones;
    }
}
