package com.enjoyit.controller;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GroupOrderControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        com.enjoyit.repository.GroupOrderRepository repository = new com.enjoyit.repository.InMemoryGroupOrderRepository();
        com.enjoyit.service.GroupOrderService service = new com.enjoyit.service.GroupOrderService(new com.enjoyit.service.PasswordValidator(), repository);
        mockMvc = MockMvcBuilders.standaloneSetup(new GroupOrderController(service, repository))
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter()
                )
                .build();
    }

    @Test
    @DisplayName("測試發起新團購：參數齊全時，應成功建立並回傳 OrderId")
    void testPublishGroupOrder_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("orderInfo", "實驗室下午茶");
        request.put("vendorId", "vendor_001");
        request.put("adminPassword", "secret123");
        request.put("groupId", "lab_group"); // 帶入正常的群組 ID

        mockMvc.perform(post("/api/group-orders/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("order_")));
    }

    @Test
    @DisplayName("測試 API 防護網：發起團購時若缺少 groupId，應回傳 400 Bad Request")
    void testPublishGroupOrder_MissingGroupId() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("orderInfo", "非法團購");
        // 🚨 刻意不放入 groupId 參數

        mockMvc.perform(post("/api/group-orders/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("缺少群組資訊"));
    }

    @Test
    @DisplayName("測試查詢所有團購：缺少 groupId 參數時，應拒絕存取")
    void testGetAllOrders_MissingGroupId() throws Exception {
        // 模擬直接呼叫 /api/group-orders/all 而不帶參數
        mockMvc.perform(get("/api/group-orders/all"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("測試點餐防護網：已結單的團購應拒絕新增餐點")
    void testAddOrderItem_RejectedWhenClosed() throws Exception {
        // 步驟 1：先發起一個團購
        Map<String, String> publishReq = new HashMap<>();
        publishReq.put("orderInfo", "測試防護網團購");
        publishReq.put("groupId", "secure_group");
        publishReq.put("adminPassword", "1234");

        // 執行發起 API，並把回傳的 orderId 抓出來存進變數
        String orderId = mockMvc.perform(post("/api/group-orders/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(publishReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 步驟 2：模擬管理員將這個團購結單
        mockMvc.perform(post("/api/group-orders/close/" + orderId + "?groupId=secure_group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"1234\"}"))
                .andExpect(status().isOk());

        // 步驟 3：模擬遲到的同學嘗試送出點餐
        String orderItemJson = "{\"participantId\":\"user_1\",\"itemName\":\"綠茶\",\"unitPrice\":30,\"quantity\":1}";

        mockMvc.perform(post("/api/group-orders/" + orderId + "/items?groupId=secure_group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderItemJson))
                // 驗證系統是否有成功擋下，回傳 400 Bad Request
                .andExpect(status().isBadRequest())
                // 驗證錯誤訊息是否正確
                .andExpect(content().string("此團購已截止，無法點餐"));
    }

    @Test
    @DisplayName("測試點餐驗證：未填寫訂購人姓名時應拒絕送出")
    void testAddOrderItem_MissingOrderFor() throws Exception {
        Map<String, String> publishReq = new HashMap<>();
        publishReq.put("orderInfo", "命名驗證團購");
        publishReq.put("groupId", "naming_group");
        publishReq.put("adminPassword", "1234");

        String orderId = mockMvc.perform(post("/api/group-orders/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(publishReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String orderItemJson = "{\"participantId\":\"device_1\",\"itemName\":\"紅茶\",\"unitPrice\":30,\"quantity\":1,\"orderTotalPrice\":30}";

        mockMvc.perform(post("/api/group-orders/" + orderId + "/items?groupId=naming_group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderItemJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("請輸入訂購人姓名"));
    }

    @Test
    @DisplayName("測試訂單項目修改與取消：應可更新既有餐點並刪除")
    void testUpdateAndDeleteOrderItem() throws Exception {
        Map<String, String> publishReq = new HashMap<>();
        publishReq.put("orderInfo", "編輯餐點團購");
        publishReq.put("groupId", "edit_group");
        publishReq.put("adminPassword", "1234");

        String orderId = mockMvc.perform(post("/api/group-orders/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(publishReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String createJson = "{\"participantId\":\"device_1\",\"orderFor\":\"王小明\",\"itemName\":\"綠茶\",\"unitPrice\":30,\"quantity\":1,\"orderTotalPrice\":30,\"customizations\":[]}";
        String createdItem = mockMvc.perform(post("/api/group-orders/" + orderId + "/items?groupId=edit_group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderFor").value("王小明"))
                .andReturn().getResponse().getContentAsString();

        String itemId = objectMapper.readTree(createdItem).get("itemID").asText();
        String updateJson = "{\"participantId\":\"device_1\",\"orderFor\":\"王小美\",\"itemName\":\"奶茶\",\"unitPrice\":40,\"quantity\":2,\"orderTotalPrice\":80,\"customizations\":[\"少冰\"]}";

        mockMvc.perform(put("/api/group-orders/" + orderId + "/items/" + itemId + "?groupId=edit_group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderFor").value("王小美"))
                .andExpect(jsonPath("$.itemName").value("奶茶"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.orderTotalPrice").value(80));

        mockMvc.perform(delete("/api/group-orders/" + orderId + "/items/" + itemId + "?groupId=edit_group"))
                .andExpect(status().isOk())
                .andExpect(content().string("餐點已取消"));
    }

    @Test
    @DisplayName("測試批次點餐：應可一次新增多筆餐點")
    void testAddOrderItemsBatch() throws Exception {
        Map<String, String> publishReq = new HashMap<>();
        publishReq.put("orderInfo", "批次點餐團購");
        publishReq.put("groupId", "batch_group");
        publishReq.put("adminPassword", "1234");

        String orderId = mockMvc.perform(post("/api/group-orders/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(publishReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

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
                    },
                    {
                      "participantId": "device_1",
                      "orderFor": "王小明",
                      "itemName": "雞排",
                      "menuItemId": "item_2",
                      "unitPrice": 75,
                      "quantity": 1,
                      "orderTotalPrice": 75,
                      "customizations": []
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/group-orders/" + orderId + "/items/batch?groupId=batch_group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemName").value("奶茶"))
                .andExpect(jsonPath("$[1].itemName").value("雞排"));
    }
}
