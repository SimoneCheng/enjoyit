package com.enjoyit.controller;

import com.enjoyit.domain.GroupOrder;
import com.enjoyit.domain.OrderItem;
import com.enjoyit.dto.DeadlineRequest;
import com.enjoyit.service.GroupOrderService;
import com.enjoyit.service.OrderSummaryGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@RestController
@RequestMapping("/api/group-orders")
public class GroupOrderController {

    private final GroupOrderService groupOrderService;

    public GroupOrderController(GroupOrderService groupOrderService) {
        this.groupOrderService = groupOrderService;
    }

    /**
     * CO-07: publishGroupOrder(orderInfo, announcement)
     * 建立新的團購活動並發布公告
     */
    @PostMapping("/publish")
    public ResponseEntity<?> publishGroupOrder(@RequestBody Map<String, String> request) {
        String orderInfo = request.get("orderInfo");
        String announcement = request.get("announcement");
        String vendorId = request.get("vendorId"); // 接收前端選擇的店家
        String adminPassword = request.get("adminPassword"); // 接收前端設定的密碼
        String groupId = request.get("groupId");

        if (groupId == null || groupId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("缺少群組資訊");
        }

        String orderId = groupOrderService.publishGroupOrder(orderInfo, announcement, vendorId, adminPassword, groupId);
        return ResponseEntity.ok(orderId);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable String orderId, @RequestParam String groupId) {
        GroupOrder order = requireOrder(orderId, groupId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<?> addOrderItem(
            @PathVariable String orderId,
            @RequestParam String groupId,
            @RequestBody OrderItem item) {
        GroupOrder order = requireOrder(orderId, groupId);
        if ("已結單".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body("此團購已截止，無法點餐");
        }

        groupOrderService.addOrderItem(order, item);
        return ResponseEntity.ok("餐點新增成功");
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders(@RequestParam String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("缺少群組資訊");
        }
        return ResponseEntity.ok(groupOrderService.getOrdersByGroupId(groupId));
    }

    @PutMapping("/{orderId}/deadline")
    public ResponseEntity<?> setOrderDeadline(
            @PathVariable String orderId,
            @RequestParam String groupId,
            @RequestParam(required = false) String password,
            @RequestBody DeadlineRequest request) {
        GroupOrder order = requireOrder(orderId, groupId);

        if (!groupOrderService.verifyAdminAccess(password, order.getAdminPassword())) {
            return ResponseEntity.status(401).body("密碼錯誤，權限不足");
        }

        String deadlineStr = request.getDeadline();
        if (deadlineStr == null || deadlineStr.trim().isEmpty()) {
            return ResponseEntity.ok("未設定截止時間");
        }
        try {
            LocalDateTime newTime = LocalDateTime.parse(deadlineStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            groupOrderService.setOrderDeadline(order, newTime);
            return ResponseEntity.ok("已更新截止時間");
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("時間格式不正確");
        }
    }

    @PostMapping("/{orderId}/validate-admin")
    public ResponseEntity<?> inputAdminPassword(
            @PathVariable String orderId,
            @RequestParam String groupId,
            @RequestBody Map<String, String> request) {
        GroupOrder order = requireOrder(orderId, groupId);

        String adminPassword = request.get("password");
        boolean isValid = groupOrderService.verifyAdminAccess(adminPassword, order.getAdminPassword());

        if (isValid) {
            return ResponseEntity.ok("AuthToken_Issued");
        }
        return ResponseEntity.status(401).body("Invalid Password");
    }

    @PostMapping("/close/{orderId}")
    public ResponseEntity<?> closeOrder(@PathVariable String orderId, @RequestParam String groupId) {
        GroupOrder order = requireOrder(orderId, groupId);
        groupOrderService.closeOrder(order);
        return ResponseEntity.ok("訂單已成功結單");
    }

    @GetMapping("/{orderId}/summary")
    public ResponseEntity<?> downloadOrderSummary(@PathVariable String orderId, @RequestParam String groupId) {
        GroupOrder order = requireOrder(orderId, groupId);

        if (!"已結單".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body("Error: Order not closed");
        }

        OrderSummaryGenerator generator = new OrderSummaryGenerator();
        return ResponseEntity.ok(generator.createSummary(order.getOrderItems()));
    }

    private GroupOrder requireOrder(String orderId, String groupId) {
        return groupOrderService.getOrderById(orderId)
                .filter(order -> groupId.equals(order.getGroupId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到該訂單"));
    }
}
