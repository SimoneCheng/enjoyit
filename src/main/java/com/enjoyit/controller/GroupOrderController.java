package com.enjoyit.controller;

import com.enjoyit.domain.GroupOrder;
import com.enjoyit.dto.DeadlineRequest;
import com.enjoyit.service.OrderSummaryGenerator;
import com.enjoyit.service.PasswordValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@RestController
@RequestMapping("/api/group-orders")
public class GroupOrderController {

    // 模擬 Heap 記憶體存儲
    private GroupOrder currentGroupOrder;
    private final PasswordValidator passwordValidator = new PasswordValidator();
    private Map<String, GroupOrder> ordersMap = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * CO-07: publishGroupOrder(orderInfo, announcement)
     * 建立新的團購活動並發布公告
     */
    @PostMapping("/publish")
    public ResponseEntity<?> publishGroupOrder(@RequestBody Map<String, String> request) {
        String orderInfo = request.get("orderInfo");
        String announcement = request.get("announcement");

        GroupOrder newOrder = new GroupOrder(orderInfo);
        // 產生一個唯一 ID (例如用 timestamp)
        String orderId = "order_" + System.currentTimeMillis();
        newOrder.setOrderId(orderId);

        if (announcement != null && !announcement.isEmpty()) {
            newOrder.setAnnouncement(announcement);
        }
        newOrder.setStatus("進行中");

        // 存入 Map
        ordersMap.put(orderId, newOrder);
        // 更新 currentGroupOrder
        this.currentGroupOrder = newOrder;

        // 回傳 ID 供前端導向
        return ResponseEntity.ok(orderId);
    }

    // 供左側列表讀取所有團購
    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(ordersMap.values());
    }

    /**
     * CO-08: setOrderDeadline(newTime)
     * 設定團購截止時間，並由領域物件判斷是否截止
     */
    @PutMapping("/deadline")
    public ResponseEntity<?> setOrderDeadline(@RequestBody DeadlineRequest request) {
        String deadlineStr = request.getDeadline();

        // 如果傳過來的是空的，直接回傳 OK 或提示，不要進行解析
        if (deadlineStr == null || deadlineStr.trim().isEmpty()) {
            return ResponseEntity.ok("未設定截止時間，將由主揪手動結單");
        }

        try {
            LocalDateTime newTime = LocalDateTime.parse(deadlineStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            if (currentGroupOrder != null) {
                currentGroupOrder.setOrderDeadline(newTime);
                return ResponseEntity.ok("已設定截止時間");
            }
            return ResponseEntity.badRequest().body("尚未建立團購活動");

        } catch (DateTimeParseException e) {
            // 捕捉解析錯誤，不讓後端當機
            return ResponseEntity.badRequest().body("時間格式不正確");
        }
    }

    /**
     * CO-09: inputAdminPassword(adminPassword)
     * 驗證管理者密碼以取得管理權限
     */
    @PostMapping("/validate-admin")
    public ResponseEntity<?> inputAdminPassword(@RequestBody Map<String, String> request) {
        String adminPassword = request.get("password");

        // 協調 PasswordValidator 進行驗證
        boolean isValid = passwordValidator.isValid(adminPassword, currentGroupOrder.getAdminPassword());

        if (isValid) {
            return ResponseEntity.ok("AuthToken_Issued");
        }
        return ResponseEntity.status(401).body("Invalid Password");
    }

    @PostMapping("/close/{orderId}")
    public ResponseEntity<?> closeOrder(@PathVariable String orderId) {
        GroupOrder order = ordersMap.get(orderId);
        if (order != null) {
            order.setStatus("已結單");
            return ResponseEntity.ok("訂單已成功結單");
        }
        return ResponseEntity.badRequest().body("找不到該訂單");
    }

    /**
     * CO-10: downloadOrderSummary()
     * 產出經過聚合處理的訂單明細總表
     */
    @GetMapping("/summary")
    public ResponseEntity<?> downloadOrderSummary() {
        // Precondition 檢查：狀態必須為已結單
        if (currentGroupOrder == null || !"已結單".equals(currentGroupOrder.getStatus())) {
            return ResponseEntity.badRequest().body("Error: Order not closed");
        }

        // 委託給 Pure Fabrication (Generator) 執行資料聚合運算
        OrderSummaryGenerator generator = new OrderSummaryGenerator();
        return ResponseEntity.ok(generator.createSummary(currentGroupOrder.getOrderItems()));
    }
}