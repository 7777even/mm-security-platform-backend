package com.sinopec.mmsecurity.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sinopec.mmsecurity.entity.SysMenu;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysMenuMapper;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;

/**
 * 稳定引用数据读穿缓存（ID↔名称一致性层）。
 *
 * <p>覆盖对象：{@code sys_user(id→realName)}、{@code sys_menu(全量列表)} 等极少变更、却在列表接口
 * 被反复解析的引用数据。采用进程内 Caffeine 缓存，TTL 5min 兜底 + 写时主动失效保证最终一致。</p>
 *
 * <p>约束（见 {@code docs/architecture/id-name-cache.md}）：</p>
 * <ul>
 *   <li>仅缓存稳定引用数据；高频变更业务实体（报警/事件）不进此缓存，每次读库保实时。</li>
 *   <li>写时失效必须覆盖所有写入口（改姓名 / 菜单管理），否则出现「改了但列表不变」。</li>
 *   <li>缓存是性能手段而非信任边界：权限判定仍基于实时 {@code UserContext}。</li>
 * </ul>
 *
 * <p>多实例部署时进程内缓存 TTL 即最终一致窗口；如需强一致改用共享缓存（Redis），当前阶段不引入。</p>
 */
@Service
@RequiredArgsConstructor
public class IdNameCacheService {

    private final SysUserMapper userMapper;
    private final SysMenuMapper menuMapper;

    private Cache<Long, String> userRealNameCache;
    private Cache<String, List<SysMenu>> menuListCache;
    private static final String MENU_ALL = "ALL";

    @PostConstruct
    void init() {
        this.userRealNameCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(2000)
                .build();
        this.menuListCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(16)
                .build();
    }

    /**
     * 用户 id → 真实姓名（读穿：未命中查库回填，TTL 5min 兜底）。
     * 返回 null 表示用户不存在或真实姓名为空。
     */
    public String userName(Long id) {
        if (id == null) {
            return null;
        }
        return userRealNameCache.get(id, k -> {
            SysUser u = userMapper.selectById(k);
            return (u != null && u.getRealName() != null) ? u.getRealName() : null;
        });
    }

    /**
     * 全量菜单（TTL 5min 缓存）。菜单变更后调用 {@link #reloadMenus()} 主动失效。
     */
    public List<SysMenu> allMenus() {
        return menuListCache.get(MENU_ALL, k -> menuMapper.selectList(null));
    }

    /** 用户姓名变更后失效对应缓存项。 */
    public void evictUser(Long id) {
        if (id != null) {
            userRealNameCache.invalidate(id);
        }
    }

    /** 菜单变更后整表失效，下次读取触发回填。 */
    public void reloadMenus() {
        menuListCache.invalidateAll();
    }
}
