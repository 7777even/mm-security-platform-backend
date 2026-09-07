package com.sinopec.mmsecurity.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 当前请求上下文中已解析的登录态。
 * 由 JwtFilter 注入，Service 层直接取用，不依赖 HttpServletRequest。
 */
@Data
@AllArgsConstructor
public class LoginUser {
    private Long userId;
    private String username;
    private String role;
}
