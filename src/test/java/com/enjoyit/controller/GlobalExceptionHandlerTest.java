package com.enjoyit.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("測試欄位驗證攔截：應回傳 400 Bad Request 並將所有錯誤訊息串接起來")
    void testHandleValidationExceptions() {
        // 1. Mock 出例外物件與 BindingResult
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        // 2. 模擬兩個欄位驗證失敗的錯誤訊息
        ObjectError error1 = new ObjectError("field1", "群組帳號不能為空");
        ObjectError error2 = new ObjectError("field2", "密碼長度至少需8碼");
        when(bindingResult.getAllErrors()).thenReturn(List.of(error1, error2));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // 3. 直接呼叫攔截器方法
        ResponseEntity<?> response = exceptionHandler.handleValidationExceptions(ex);

        // 4. 驗證回傳的 HTTP 狀態碼與 JSON 內容
        assertEquals(400, response.getStatusCode().value());

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("群組帳號不能為空, 密碼長度至少需8碼", responseBody.get("error"));
    }
}