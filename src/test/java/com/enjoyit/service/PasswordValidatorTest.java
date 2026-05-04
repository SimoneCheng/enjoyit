package com.enjoyit.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    private final PasswordValidator validator = new PasswordValidator();

    @Test
    @DisplayName("測試密碼驗證：密碼完全一致時應回傳 true")
    void testValidPassword() {
        assertTrue(validator.isValid("secret123", "secret123"));
    }

    @Test
    @DisplayName("測試密碼驗證：密碼錯誤時應回傳 false")
    void testInvalidPassword() {
        assertFalse(validator.isValid("wrong_password", "secret123"));
    }

    @Test
    @DisplayName("測試密碼驗證：輸入為 null 時應能安全處理並回傳 false")
    void testNullPassword() {
        assertFalse(validator.isValid(null, "secret123"));
        assertFalse(validator.isValid("secret123", null));
        assertFalse(validator.isValid(null, null));
    }
}