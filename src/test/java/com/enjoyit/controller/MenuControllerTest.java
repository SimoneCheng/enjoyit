package com.enjoyit.controller;

import com.enjoyit.domain.Menu;
import com.enjoyit.service.MenuService;
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
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MenuControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MenuService menuService;

    @InjectMocks
    private MenuController menuController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(menuController)
                // 👇 直接抽換底層的訊息轉換器，強制使用 UTF-8 處理字串
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter()
                )
                .build();
    }

    @Test
    @DisplayName("測試提交菜單草稿 (POST)：應成功呼叫 Service 並回傳 200 OK")
    void testSubmitMenuCreation() throws Exception {
        Menu mockMenu = new Menu(); // 建立空菜單

        mockMvc.perform(post("/api/vendors/vendor_001/menu/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockMenu)))
                .andExpect(status().isOk())
                .andExpect(content().string("菜單新增成功"));

        // 驗證確實有把請求轉交給 Service 處理
        verify(menuService, times(1)).submitMenuCreation(eq("vendor_001"), any(Menu.class));
    }

    @Test
    @DisplayName("測試取得店家菜單 (GET)：應成功向 Service 索取資料並回傳")
    void testFetchMenuData() throws Exception {
        Menu mockMenu = new Menu();
        when(menuService.fetchMenuData("vendor_001")).thenReturn(mockMenu);

        mockMvc.perform(get("/api/vendors/vendor_001/menu"))
                .andExpect(status().isOk());

        verify(menuService, times(1)).fetchMenuData("vendor_001");
    }

    @Test
    @DisplayName("測試更新單一品項 (PATCH)：應正確接收選填的 RequestParam")
    void testUpdateMenuItem() throws Exception {
        // 模擬前端發送 PATCH 請求並帶入 @RequestParam
        mockMvc.perform(patch("/api/vendors/vendor_001/menu/items/item_01")
                        .param("newPrice", "65")
                        .param("isActive", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string("品項更新成功"));

        // 驗證 Controller 有將字串參數正確轉換並傳給 Service
        verify(menuService, times(1)).updateMenuItem("vendor_001", "item_01", 65, false);
    }

    @Test
    @DisplayName("測試更新單一品項 (PATCH)：只更新狀態不改價格時也應成功")
    void testUpdateMenuItem_PartialUpdate() throws Exception {
        // 刻意不傳入 newPrice
        mockMvc.perform(patch("/api/vendors/vendor_001/menu/items/item_01")
                        .param("isActive", "true"))
                .andExpect(status().isOk());

        // 驗證 newPrice 會以 null 的形式傳遞
        verify(menuService, times(1)).updateMenuItem("vendor_001", "item_01", null, true);
    }
}