package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.LoginRequest;
import com.sinopec.mmsecurity.dto.TokenResponse;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import com.sinopec.mmsecurity.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder;

    @Value("${jwt.access-ttl}")
    private long accessTtl;

    /** 开发环境默认菜单（实际从 sys_menu 表加载） */
    private static final List<Map<String, Object>> MENUS = List.of(
            Map.of("id", 1, "name", "首页", "code", "dashboard", "path", "/dashboard"),
            Map.of("id", 2, "name", "设施管理", "code", "facility", "path", "/facility"),
            Map.of("id", 3, "name", "告警中心", "code", "alarm", "path", "/alarm"),
            Map.of("id", 4, "name", "设备台账", "code", "device", "path", "/device"),
            Map.of("id", 5, "name", "应急指挥", "code", "emergency", "path", "/emergency")
    );

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
                jwtUtil.issueRefresh(u.getUsername()),
                accessTtl);
    }

    public TokenResponse refresh(String refreshToken) {
        var claims = jwtUtil.parse(refreshToken);
        if (claims == null || !"refresh".equals(claims.get("type", String.class))) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "refresh 令牌无效");
        }
        String username = claims.getSubject();
        return TokenResponse.of(
                jwtUtil.issueAccess(username, "ADMIN"),
                jwtUtil.issueRefresh(username),
                accessTtl);
    }

    public Map<String, Object> me() {
        return Map.of("username", "admin", "realName", "系统管理员", "role", "ADMIN");
    }

    public List<Map<String, Object>> menus() {
        return MENUS;
    }
}
