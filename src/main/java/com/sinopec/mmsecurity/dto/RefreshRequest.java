package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新令牌请求体。契约 auth.openapi.json 的 POST /auth/refresh 定义为 { refreshToken: string }（required）。
 */
@Data
public class RefreshRequest {
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
