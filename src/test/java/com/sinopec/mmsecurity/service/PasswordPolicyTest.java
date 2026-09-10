package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PasswordPolicy（纯 POJO）：复杂度策略与随机临时口令生成。
 */
class PasswordPolicyTest {

    private final PasswordPolicy policy = new PasswordPolicy();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(policy, "enabled", true);
        ReflectionTestUtils.setField(policy, "minLength", 8);
        ReflectionTestUtils.setField(policy, "requireCategories", 3);
    }

    @Test
    void tooShort_rejected() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> policy.validate("admin", "Ab1!", null));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
    }

    @Test
    void tooFewCategories_rejected() {
        // 仅小写一类
        assertThrows(BusinessException.class, () -> policy.validate("admin", "abcdefghij", null));
    }

    @Test
    void containsUsername_rejected() {
        assertThrows(BusinessException.class, () -> policy.validate("zhangsan", "Zhangsan1!", null));
    }

    @Test
    void sameAsOld_rejected() {
        assertThrows(BusinessException.class, () -> policy.validate("admin", "Passw0rd!", "Passw0rd!"));
    }

    @Test
    void validPassword_passes() {
        assertDoesNotThrow(() -> policy.validate("admin", "Passw0rd!", "Oldpass1!"));
    }

    @Test
    void disabledPolicy_skipsComplexityChecks() {
        ReflectionTestUtils.setField(policy, "enabled", false);
        assertDoesNotThrow(() -> policy.validate("admin", "weak", null));
    }

    @Test
    void generatedPassword_meetsComplexity_andIsRandom() {
        String a = policy.generate();
        String b = policy.generate();
        assertEquals(12, a.length(), "临时口令长度应为 12");
        assertTrue(PasswordPolicy.categories(a) >= 3, "临时口令应至少覆盖 3 类字符");
        assertNotEquals(a, b, "两次生成不应相同（随机性）");
        assertTrue(a.chars().anyMatch(Character::isUpperCase), "应含大写字母");
        assertTrue(a.chars().anyMatch(Character::isDigit), "应含数字");
    }
}
