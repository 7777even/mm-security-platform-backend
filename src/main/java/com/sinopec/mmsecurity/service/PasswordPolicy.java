package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Locale;

/**
 * 口令策略（配置化）：长度、字符类别、不含用户名、不与旧口令相同。
 *
 * <p>配置项（{@code app.password.*}，见 application.yml）：</p>
 * <ul>
 *   <li>{@code enabled}：总开关，默认 true；关停仅用于应急，正式环境必须开启。</li>
 *   <li>{@code min-length}：最短长度，默认 8。</li>
 *   <li>{@code require-categories}：须满足的字符类别数（大写/小写/数字/符号），默认 3。</li>
 * </ul>
 *
 * <p>哈希方式不在此处——沿用 {@code BCryptPasswordEncoder}（password-security.md §1），本类只做「接受与否」的判定。</p>
 */
@Service
public class PasswordPolicy {

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGIT = "23456789";
    private static final String SYMBOL = "!@#$%^&*-_=+";
    private static final int GENERATED_LENGTH = 12;

    private final SecureRandom random = new SecureRandom();

    @Value("${app.password.enabled:true}")
    private boolean enabled;

    @Value("${app.password.min-length:8}")
    private int minLength;

    @Value("${app.password.require-categories:3}")
    private int requireCategories;

    /**
     * 校验新口令；不通过即抛 {@link ResultCode#PARAM_INVALID}（HTTP 200 + code=100，前端按 message 提示）。
     *
     * @param username    账号（用于「不得含用户名」校验，可为空）
     * @param newPassword 待设置的口令
     * @param oldPassword 旧口令（可为空；非空时校验「不得与原密码相同」）
     */
    public void validate(String username, String newPassword, String oldPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "新密码不能为空");
        }
        if (!enabled) {
            return;
        }
        if (newPassword.length() < minLength) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "新密码长度不得少于 " + minLength + " 位");
        }
        if (categories(newPassword) < requireCategories) {
            throw new BusinessException(ResultCode.PARAM_INVALID,
                    "新密码须至少包含大写字母、小写字母、数字、符号中的 " + requireCategories + " 类");
        }
        if (username != null && !username.isBlank()
                && newPassword.toLowerCase(Locale.ROOT).contains(username.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "新密码不得包含用户名");
        }
        if (oldPassword != null && !oldPassword.isBlank() && newPassword.equals(oldPassword)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "新密码不得与原密码相同");
        }
    }

    /**
     * 生成随机临时口令（管理员重置用）：长度 12，保证四类字符各至少一个后随机补足，再洗牌。
     * 绝不用固定值（design §7.2）。
     */
    public String generate() {
        StringBuilder sb = new StringBuilder(GENERATED_LENGTH);
        sb.append(pick(UPPER)).append(pick(LOWER)).append(pick(DIGIT)).append(pick(SYMBOL));
        String pool = UPPER + LOWER + DIGIT + SYMBOL;
        while (sb.length() < GENERATED_LENGTH) {
            sb.append(pick(pool));
        }
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }

    /** 统计口令涵盖的字符类别数（大写 / 小写 / 数字 / 符号）。 */
    static int categories(String password) {
        int count = 0;
        if (password.chars().anyMatch(Character::isUpperCase)) count++;
        if (password.chars().anyMatch(Character::isLowerCase)) count++;
        if (password.chars().anyMatch(Character::isDigit)) count++;
        if (password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch))) count++;
        return count;
    }

    private char pick(String pool) {
        return pool.charAt(random.nextInt(pool.length()));
    }
}
