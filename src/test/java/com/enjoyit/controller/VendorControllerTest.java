package com.enjoyit.controller;

import com.enjoyit.domain.Vendor;
import com.enjoyit.service.VendorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VendorController.class)
class VendorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VendorService vendorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/vendors：應回傳所有店家資訊，供後台管理使用")
    void testGetAllVendors() throws Exception {
        Vendor v = new Vendor("店家A", "02-123", "地址A");
        when(vendorService.getAllVendors()).thenReturn(List.of(v));

        mockMvc.perform(get("/api/vendors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("店家A"));
    }

    @Test
    @DisplayName("POST /api/vendors：成功新增店家後應可全域使用")
    void testCreateVendor() throws Exception {
        Map<String, String> request = Map.of(
            "name", "全域店家",
            "phone", "0911",
            "address", "台北市"
        );

        mockMvc.perform(post("/api/vendors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(vendorService, times(1)).createVendor(eq("全域店家"), eq("0911"), eq("台北市"));
    }

    @Test
    @DisplayName("PUT /api/vendors/{id}：更新店家資訊應同步影響所有群組")
    void testUpdateVendor() throws Exception {
        Map<String, String> request = Map.of(
            "name", "更名店家",
            "phone", "0922",
            "address", "新地址"
        );

        mockMvc.perform(put("/api/vendors/v123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(vendorService, times(1)).updateVendor(eq("v123"), eq("更名店家"), eq("0922"), eq("新地址"));
    }

    @Test
    @DisplayName("DELETE /api/vendors/{id}：下架店家後應無法再被新群組選擇")
    void testDeleteVendor() throws Exception {
        mockMvc.perform(delete("/api/vendors/v123"))
                .andExpect(status().isOk())
                .andExpect(content().string("店家已成功下架"));

        verify(vendorService, times(1)).deleteVendor("v123");
    }

    @Test
    @DisplayName("POST /api/vendors/{id}/status：有進行中訂單時下架應回傳 400 並提示錯誤")
    void testSetVendorStatus_OngoingOrders() throws Exception {
        doThrow(new IllegalStateException("有團購在使用此店家，不得下架或刪除"))
            .when(vendorService).setVendorActiveStatus("v123", false);

        mockMvc.perform(post("/api/vendors/v123/status?active=false"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("有團購在使用此店家，不得下架或刪除"));
    }
}