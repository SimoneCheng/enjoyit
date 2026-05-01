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
    // 1. 更新發布團購 API
    @PostMapping("/publish")
    public ResponseEntity<?> publishGroupOrder(@RequestBody Map<String, String> request) {
        String orderInfo = request.get("orderInfo");
        String announcement = request.get("announcement");
        String vendorId = request.get("vendorId"); // 接收前端選擇的店家
        String adminPassword = request.get("adminPassword"); // 接收前端設定的密碼

        GroupOrder newOrder = new GroupOrder(orderInfo);
        String orderId = "order_" + System.currentTimeMillis();
        newOrder.setOrderId(orderId);
        newOrder.setVendorId(vendorId);
        newOrder.setAdminPassword(adminPassword); // 正式綁定主揪設定的密碼

        if (announcement != null && !announcement.isEmpty()) {
            newOrder.setAnnouncement(announcement);
        }

        ordersMap.put(orderId, newOrder);
        this.currentGroupOrder = newOrder;

        return ResponseEntity.ok(orderId);
    }

    // 2. 新增：查詢單一訂單資訊 (為了讓前端抓取綁定的 vendorId 與截止時間)
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable String orderId) {
        GroupOrder order = ordersMap.get(orderId);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }

    // 3. 新增：參與者送出點餐的 API
    @PostMapping("/{orderId}/items")
    public ResponseEntity<?> addOrderItem(@PathVariable String orderId, @RequestBody com.enjoyit.domain.OrderItem item) {
        GroupOrder order = ordersMap.get(orderId);
        if (order == null) {
            return ResponseEntity.badRequest().body("找不到該團購活動");
        }
        if ("已結單".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body("此團購已截止，無法點餐");
        }

        order.getOrderItems().add(item);
        return ResponseEntity.ok("餐點新增成功");
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
    // 4. 更新：設定截止時間 (必須針對特定 orderId，不然多個團購會打架)
    // 升級版：設定截止時間 (需透過 URL 參數夾帶密碼驗證)
    @PutMapping("/{orderId}/deadline")
    public ResponseEntity<?> setOrderDeadline(
            @PathVariable String orderId,
            @RequestParam(required = false) String password, // 新增密碼參數
            @RequestBody DeadlineRequest request) {

        GroupOrder order = ordersMap.get(orderId);
        if (order == null) return ResponseEntity.badRequest().body("找不到該訂單");

        // 後端親自驗證密碼
        if (!passwordValidator.isValid(password, order.getAdminPassword())) {
            return ResponseEntity.status(401).body("密碼錯誤，權限不足");
        }

        String deadlineStr = request.getDeadline();
        if (deadlineStr == null || deadlineStr.trim().isEmpty()) {
            return ResponseEntity.ok("未設定截止時間");
        }
        try {
            LocalDateTime newTime = LocalDateTime.parse(deadlineStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            order.setOrderDeadline(newTime);
            return ResponseEntity.ok("已更新截止時間");
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("時間格式不正確");
        }
    }

    /**
     * CO-09: inputAdminPassword (升級版：針對特定 orderId 驗證)
     */
    @PostMapping("/{orderId}/validate-admin")
    public ResponseEntity<?> inputAdminPassword(@PathVariable String orderId, @RequestBody Map<String, String> request) {
        GroupOrder order = ordersMap.get(orderId);
        if (order == null) {
            return ResponseEntity.badRequest().body("找不到該訂單");
        }

        String adminPassword = request.get("password");
        // 比對該特定訂單的密碼
        boolean isValid = passwordValidator.isValid(adminPassword, order.getAdminPassword());

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
     * CO-10: downloadOrderSummary (升級版：針對特定 orderId 產出總表)
     */
    @GetMapping("/{orderId}/summary")
    public ResponseEntity<?> downloadOrderSummary(@PathVariable String orderId) {
        GroupOrder order = ordersMap.get(orderId);
        if (order == null) {
            return ResponseEntity.badRequest().body("找不到該訂單");
        }

        if (!"已結單".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body("Error: Order not closed");
        }

        OrderSummaryGenerator generator = new OrderSummaryGenerator();
        return ResponseEntity.ok(generator.createSummary(order.getOrderItems()));
    }
}