package com.enjoyit.controller;

import com.enjoyit.service.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupController groupController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupController).build();
    }

    @Test
    @DisplayName("測試群組登入：帳號密碼正確時，回傳 200 OK 與 groupId")
    void testLogin_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("id", "my_group");
        request.put("password", "correct_pw");

        // 模擬 Service 驗證成功
        when(groupService.login("my_group", "correct_pw")).thenReturn(true);

        mockMvc.perform(post("/api/groups/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("登入成功"))
                .andExpect(jsonPath("$.groupId").value("my_group"));
    }

    @Test
    @DisplayName("測試群組登入：密碼錯誤時，回傳 401 Unauthorized")
    void testLogin_Failure() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("id", "my_group");
        request.put("password", "wrong_pw");

        // 模擬 Service 驗證失敗
        when(groupService.login("my_group", "wrong_pw")).thenReturn(false);

        mockMvc.perform(post("/api/groups/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("帳號或密碼錯誤"));
    }
}