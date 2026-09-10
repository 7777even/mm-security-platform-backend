package com.sinopec.mmsecurity.common.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 「是否需强制改密」状态缓存（username → must_change_pwd）。
 *
 * <p>供口令生命周期拦截器在每次**变更类**请求上判定，避免每请求查库。
 * 采用 Caffeine 读穿 + TTL 5min 兜底；改密 / 重置密码**必须**调用
 * {@link #evict(String)} 主动失效，否则会出现「已改密但仍被拦」或反向的「已重置仍未拦」。</p>
 *
 * <p>本缓存只做性能手段，判定的语义真源仍是 {@code sys_user.must_change_pwd}。</p>
 */
@Service
@RequiredArgsConstructor
public class PasswordStateCache {

    private final SysUserMapper userMapper;

    private Cache<String, Boolean> cache;

    @PostConstruct
    void init() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(2000)
                .build();
    }

    /** 该用户是否处于「须先改密」状态；未知用户返回 false（由鉴权层另行拦截）。 */
    public boolean mustChange(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        Boolean v = cache.get(username, this::load);
        return Boolean.TRUE.equals(v);
    }

    /** 改密 / 重置密码后失效，令下次判定回源。 */
    public void evict(String username) {
        if (username != null && !username.isBlank() && cache != null) {
            cache.invalidate(username);
        }
    }

    private Boolean load(String username) {
        SysUser u = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        return u != null && u.getMustChangePwd() != null && u.getMustChangePwd() == 1;
    }
}
