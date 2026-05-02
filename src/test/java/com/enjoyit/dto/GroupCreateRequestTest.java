package com.enjoyit.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupCreateRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        // 啟動一個驗證器工廠來測試 Validation 標籤
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("測試欄位驗證：合法輸入時不應有任何錯誤")
    void testValidRequest() {
        GroupCreateRequest request = new GroupCreateRequest();
        request.setId("my_group_01");
        request.setPassword("strong_password123");

        Set<ConstraintViolation<GroupCreateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "所有欄位皆合法，不應產生錯誤");
    }

    @Test
    @DisplayName("測試欄位驗證：群組帳號為空時，應觸發 @NotBlank 錯誤")
    void testBlankId() {
        GroupCreateRequest request = new GroupCreateRequest();
        request.setId(""); // 刻意設為空字串
        request.setPassword("strong_password123");

        Set<ConstraintViolation<GroupCreateRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("群組帳號不可為空", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("測試欄位驗證：密碼長度不足 8 碼時，應觸發 @Size 錯誤")
    void testShortPassword() {
        GroupCreateRequest request = new GroupCreateRequest();
        request.setId("my_group_01");
        request.setPassword("12345"); // 只有 5 碼

        Set<ConstraintViolation<GroupCreateRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("密碼長度需至少 8 碼", violations.iterator().next().getMessage());
    }
}