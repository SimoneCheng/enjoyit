package com.enjoyit.controller;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class PageControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 建立一個簡易的視圖解析器，模擬 Thymeleaf 的行為
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/"); // 設定前綴
        viewResolver.setSuffix(".html");       // 設定後綴

        mockMvc = MockMvcBuilders.standaloneSetup(new PageController())
                .setViewResolvers(viewResolver) // 把解析器裝上去
                .build();
    }

    @Test
    @DisplayName("測試 Dashboard 頁面路由")
    void testDashboardPage() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }

    @Test
    @DisplayName("測試發起團購頁面路由")
    void testPublishPage() throws Exception {
        mockMvc.perform(get("/dashboard/publish"))
                .andExpect(status().isOk())
                .andExpect(view().name("publish"));
    }

    @Test
    @DisplayName("測試店家菜單管理頁面路由")
    void testMenuPage() throws Exception {
        mockMvc.perform(get("/dashboard/menu"))
                .andExpect(status().isOk())
                .andExpect(view().name("menu"));
    }

    @Test
    @DisplayName("測試團購訂單頁面路由")
    void testOrdersPage() throws Exception {
        mockMvc.perform(get("/dashboard/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"));
    }
}