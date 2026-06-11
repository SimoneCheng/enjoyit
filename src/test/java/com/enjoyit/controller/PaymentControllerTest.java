package com.enjoyit.controller;

import com.enjoyit.repository.InMemoryGroupOrderRepository;
import com.enjoyit.service.GroupOrderService;
import com.enjoyit.service.PasswordValidator;
import com.enjoyit.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String orderId;

    @BeforeEach
    void setUp() throws Exception {
        InMemoryGroupOrderRepository repository = new InMemoryGroupOrderRepository();
        GroupOrderService groupOrderService = new GroupOrderService(new PasswordValidator(), repository);
        PaymentService paymentService = new PaymentService(repository);

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new GroupOrderController(groupOrderService),
                        new PaymentController(paymentService)
                )
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter()
                )
                .build();

        Map<String, String> publishReq = new HashMap<>();
        publishReq.put("orderInfo", "付款測試團購");
        publishReq.put("groupId", "pay_group");
        publishReq.put("adminPassword", "admin_pwd");
        publishReq.put("vendorId", "vendor_001");

        orderId = mockMvc.perform(post("/api/group-orders/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(publishReq)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String batchJson = """
                {
                  "items": [
                    {
                      "participantId": "device_1",
                      "orderFor": "王小明",
                      "itemName": "奶茶",
                      "menuItemId": "item_1",
                      "unitPrice": 50,
                      "quantity": 1,
                      "orderTotalPrice": 50,
                      "customizations": []
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/group-orders/" + orderId + "/items/batch?groupId=pay_group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson))
                .andExpect(status().isOk());
    }

    @Test
    void testGetFinanceSummary_Success() throws Exception {
        mockMvc.perform(get("/api/group-orders/" + orderId + "/payments/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].payerName").value("王小明"))
                .andExpect(jsonPath("$[0].amountDue").value(50))
                .andExpect(jsonPath("$[0].status").value("未付款"));
    }

    @Test
    void testGetFinanceSummary_WhenOrderMissing() throws Exception {
        mockMvc.perform(get("/api/group-orders/order_missing/payments/summary"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("找不到該訂單"));
    }

    @Test
    void testUpdatePaymentStatus_Success() throws Exception {
        mockMvc.perform(put("/api/group-orders/" + orderId + "/payments/status")
                        .param("payerName", "王小明")
                        .param("status", "已付款")
                        .param("remarks", "備註")
                        .param("password", "admin_pwd"))
                .andExpect(status().isOk())
                .andExpect(content().string("狀態與備註更新成功"));

        mockMvc.perform(get("/api/group-orders/" + orderId + "/payments/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("已付款"))
                .andExpect(jsonPath("$[0].remarks").value("備註"));
    }

    @Test
    void testUpdatePaymentStatus_WrongPassword() throws Exception {
        mockMvc.perform(put("/api/group-orders/" + orderId + "/payments/status")
                        .param("payerName", "王小明")
                        .param("status", "已付款")
                        .param("remarks", "備註")
                        .param("password", "wrong_pwd"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("密碼錯誤，拒絕修改財務狀態！"));
    }
}
