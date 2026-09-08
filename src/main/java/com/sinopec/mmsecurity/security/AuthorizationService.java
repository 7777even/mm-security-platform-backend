package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 越权校验服务（水平 + 垂直）。
 * <ul>
 *   <li>垂直越权（角色）：{@link #assertAdmin()} 要求当前用户为 ADMIN；</li>
 *   <li>水平越权（资源归属）：{@link #assertSelfOrAdmin(String)} 要求目标资源归属当前用户本人，
 *       或当前用户为管理员，否则拒绝（403）。</li>
 * </ul>
 * 与 {@code @RequireAuth(role=...)} 拦截器互补：注解负责端点级角色门禁，
 * 本服务负责资源级归属校验（如现场回传声明的 reporter 必须是本人）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    /** 垂直越权：要求管理员角色，否则 403 */
    public void assertAdmin() {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "需要管理员权限");
        }
    }

    /**
     * 水平越权：targetUser 必须为当前登录用户本人，或当前用户为管理员。
     * targetUser 为 null 时视为未声明归属，放行（由调用方随后绑定为本人）。
     *
     * @param targetUser 客户端声明的资源归属用户（可空）
     */
    public void assertSelfOrAdmin(String targetUser) {
        if (targetUser == null) {
            return;
        }
        String current = UserContext.username();
        if (current == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录或令牌过期");
        }
        boolean self = targetUser.equals(current);
        if (!self && !UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问他人资源");
        }
    }
}
