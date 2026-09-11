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

/**
 * 令牌失效版本号服务（V45）：在用户维度维护 {@code sys_user.token_version}，
 * 让无状态 JWT 具备「服务端可吊销」的能力。
 *
 * <p><b>解决什么问题</b>：JWT 签发后服务端不存状态，此前 {@code /auth/logout} 只清客户端 Cookie，
 * access token 在其剩余有效期（默认 2 小时）内依然能通过签名校验；
 * refresh token 有效期 7 天，一旦被窃取可持续续期。有了版本号后：
 * 登出 / 改密 / 管理员下线 → 版本号递增 → 旧令牌在 {@link JwtFilter} 校验时因版本落后被拒。</p>
 *
 * <p><b>为什么不是黑名单表</b>：黑名单要存 jti 且需清理过期项；版本号只需一个整数列，
 * 天然随用户走，改密、禁用、登出都能复用同一机制。</p>
 *
 * <p><b>缓存</b>：版本号走 Caffeine 读穿缓存（TTL 5min），避免每个请求都查库；
 * 递增时主动失效，使登出立即生效。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenVersionService {

    private final SysUserMapper userMapper;

    private volatile Cache<String, Integer> versionCache;

    @PostConstruct
    void init() {
        cache();
    }

    /**
     * 惰性建缓存：@PostConstruct 在 Spring 容器下预热，但纯 Mockito 单测不走容器，
     * 若只靠 @PostConstruct 则 versionCache 为 null 而 NPE。惰性化后两种环境都可用。
     */
    private Cache<String, Integer> cache() {
        if (versionCache == null) {
            versionCache = Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofMinutes(5))
                    .maximumSize(1024)
                    .build();
        }
        return versionCache;
    }

    /**
     * 取用户当前有效版本号。
     *
     * @return 版本号；用户不存在或无版本号时返回 0（与「老令牌无 ver claim」的兜底语义一致，
     *         不至于因查不到用户就直接拒绝——用户被删除的场景由角色/权限校验兜底）
     */
    public int current(String username) {
        if (username == null || username.isBlank()) {
            return 0;
        }
        Integer v = cache().get(username, this::load);
        return v == null ? 0 : v;
    }

    /**
     * 递增用户版本号，使此前签发的所有 access token 立即失效。
     * 用于登出、改密、管理员强制下线。
     *
     * @return 递增后的版本号；用户不存在时返回 0（无可吊销对象）
     */
    public int bump(String username) {
        if (username == null || username.isBlank()) {
            return 0;
        }
        SysUser u = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (u == null) {
            return 0;
        }
        int next = (u.getTokenVersion() == null ? 0 : u.getTokenVersion()) + 1;
        SysUser upd = new SysUser();
        upd.setId(u.getId());
        upd.setTokenVersion(next);
        userMapper.updateById(upd);
        cache().invalidate(username);
        log.info("用户 {} 令牌版本号递增至 {}（此前签发的 access token 失效）", username, next);
        return next;
    }

    private Integer load(String username) {
        SysUser u = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (u == null || u.getTokenVersion() == null) {
            return 0;
        }
        return u.getTokenVersion();
    }
}
