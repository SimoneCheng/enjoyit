package com.enjoyit.controller;

import com.enjoyit.repository.InMemoryGroupOrderRepository;
import com.enjoyit.service.GroupOrderService;
import com.enjoyit.service.PasswordValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GroupOrderControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        PasswordValidator passwordValidator = new PasswordValidator();
        InMemoryGroupOrderRepository repository = new InMemoryGroupOrderRepository();
        GroupOrderService groupOrderService = new GroupOrderService(passwordValidator, repository);

        mockMvc = MockMvcBuilders.standaloneSetup(new GroupOrderController(groupOrderService))
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
        request.put("groupId", "lab_group");

        mockMvc.perform(post("/api/group-orders/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(startsWith("order_")));
    }

    @Test
    @DisplayName("測試 API 防護網：發起團購時若缺少 groupId，應回傳 400")
    void testPublishGroupOrder_MissingGroupId() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("orderInfo", "非法團購");

        mockMvc.perform(post("/api/group-orders/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("缺少群組資訊"));
    }

    @Test
    @DisplayName("測試查詢所有團購：缺少 groupId 參數時，應拒絕存取")
    void testGetAllOrders_MissingGroupId() throws Exception {
        mockMvc.perform(get("/api/group-orders/all"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("測試點餐防護網：已結單的團購應拒絕新增餐點")
    void testAddOrderItem_RejectedWhenClosed() throws Exception {
        Map<String, String> publishReq = new HashMap<>();
        publishReq.put("orderInfo", "測試防護網團購");
        publishReq.put("groupId", "secure_group");
        publishReq.put("adminPassword", "1234");
        publishReq.put("vendorId", "vendor_001");

        String orderId = mockMvc.perform(post("/api/group-orders/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(publishReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post("/api/group-orders/close/" + orderId + "?groupId=secure_group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"1234\"}"))
                .andExpect(status().isOk());

        String orderItemJson = "{\"participantId\":\"user_1\",\"itemName\":\"綠茶\",\"unitPrice\":30,\"quantity\":1}";

        mockMvc.perform(post("/api/group-orders/" + orderId + "/items?groupId=secure_group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderItemJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("此團購已截止，無法點餐"));
    }

    @Test
    @DisplayName("測試點餐驗證：未填寫訂購人姓名時應拒絕送出")
    void testAddOrderItem_MissingOrderFor() throws Exception {
        Map<String, String> publishReq = new HashMap<>();
        publishReq.put("orderInfo", "命名驗證團購");
        publishReq.put("groupId", "naming_group");
        publishReq.put("adminPassword", "1234");
        publishReq.put("vendorId", "vendor_001");

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
        publishReq.put("vendorId", "vendor_001");

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
        publishReq.put("vendorId", "vendor_001");

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
