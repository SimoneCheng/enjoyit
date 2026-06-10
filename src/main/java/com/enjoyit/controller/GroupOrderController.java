package com.enjoyit.controller;

import com.enjoyit.domain.GroupOrder;
import com.enjoyit.domain.OrderItem;
import com.enjoyit.dto.OrderItemsBatchRequest;
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
import java.util.List;
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
        String closedMessage = getClosedOrderMessage(order);
        if (closedMessage != null) {
            return ResponseEntity.badRequest().body(closedMessage);
        }
        String validationMessage = validateOrderItem(item);
        if (validationMessage != null) {
            return ResponseEntity.badRequest().body(validationMessage);
        }

        groupOrderService.addOrderItem(order, item);
        return ResponseEntity.ok(item);
    }

    @PostMapping("/{orderId}/items/batch")
    public ResponseEntity<?> addOrderItemsBatch(
            @PathVariable String orderId,
            @RequestParam String groupId,
            @RequestBody OrderItemsBatchRequest request) {
        GroupOrder order = requireOrder(orderId, groupId);
        String closedMessage = getClosedOrderMessage(order);
        if (closedMessage != null) {
            return ResponseEntity.badRequest().body(closedMessage);
        }
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body("待送出的餐點不能為空");
        }

        for (OrderItem item : request.getItems()) {
            String validationMessage = validateOrderItem(item);
            if (validationMessage != null) {
                return ResponseEntity.badRequest().body(validationMessage);
            }
        }

        groupOrderService.addOrderItems(order, request.getItems());
        return ResponseEntity.ok(request.getItems());
    }

    @PutMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<?> updateOrderItem(
            @PathVariable String orderId,
            @PathVariable String itemId,
            @RequestParam String groupId,
            @RequestBody OrderItem updatedItem) {
        GroupOrder order = requireOrder(orderId, groupId);
        String closedMessage = getClosedOrderMessage(order);
        if (closedMessage != null) {
            return ResponseEntity.badRequest().body(closedMessage);
        }
        String validationMessage = validateOrderItem(updatedItem);
        if (validationMessage != null) {
            return ResponseEntity.badRequest().body(validationMessage);
        }

        OrderItem existingItem = findOrderItem(order.getOrderItems(), itemId);
        groupOrderService.updateOrderItem(order, existingItem, updatedItem);
        return ResponseEntity.ok(existingItem);
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<?> deleteOrderItem(
            @PathVariable String orderId,
            @PathVariable String itemId,
            @RequestParam String groupId) {
        GroupOrder order = requireOrder(orderId, groupId);
        String closedMessage = getClosedOrderMessage(order);
        if (closedMessage != null) {
            return ResponseEntity.badRequest().body(closedMessage);
        }

        OrderItem existingItem = findOrderItem(order.getOrderItems(), itemId);
        groupOrderService.deleteOrderItem(order, existingItem);
        return ResponseEntity.ok("餐點已取消");
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders(@RequestParam String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("缺少群組資訊");
        }
        return ResponseEntity.ok(groupOrderService.getOrdersByGroupId(groupId));
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
            @RequestParam String groupId,
            @RequestParam(required = false) String password, // 新增密碼參數
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

    /**
     * CO-09: inputAdminPassword (升級版：針對特定 orderId 驗證)
     */
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
    public ResponseEntity<?> closeOrder(
            @PathVariable String orderId,
            @RequestParam String groupId,
            @RequestBody(required = false) Map<String, String> request) {
        GroupOrder order = requireOrder(orderId, groupId);
        String password = request == null ? null : request.get("password");
        if (!groupOrderService.verifyAdminAccess(password, order.getAdminPassword())) {
            return ResponseEntity.status(401).body("密碼錯誤，權限不足");
        }
        groupOrderService.closeOrder(order);
        return ResponseEntity.ok("訂單已成功結單");
    }

    /**
     * CO-10: downloadOrderSummary (升級版：針對特定 orderId 產出總表)
     */
    @GetMapping("/{orderId}/summary")
    public ResponseEntity<?> downloadOrderSummary(
            @PathVariable String orderId,
            @RequestParam String groupId,
            @RequestParam(required = false) String password) {
        GroupOrder order = requireOrder(orderId, groupId);
        if (!groupOrderService.verifyAdminAccess(password, order.getAdminPassword())) {
            return ResponseEntity.status(401).body("密碼錯誤，權限不足");
        }

        if (!"已結單".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body("Error: Order not closed");
        }

        OrderSummaryGenerator generator = new OrderSummaryGenerator();
        return ResponseEntity.ok(generator.createDetailedSummary(order.getOrderItems()));
    }

    private GroupOrder requireOrder(String orderId, String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少群組資訊");
        }
        return groupOrderService.getOrderById(orderId)
                .filter(order -> groupId.equals(order.getGroupId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到該訂單"));
    }

    private String getClosedOrderMessage(GroupOrder order) {
        if ("已結單".equals(order.getStatus())) {
            return "此團購已截止，無法點餐";
        }
        return null;
    }

    private String validateOrderItem(OrderItem item) {
        if (item == null) {
            return "缺少餐點資料";
        }
        if (item.getParticipantId() == null || item.getParticipantId().trim().isEmpty()) {
            return "缺少操作裝置識別";
        }
        if (item.getOrderFor() == null || item.getOrderFor().trim().isEmpty()) {
            return "請輸入訂購人姓名";
        }
        if (item.getItemName() == null || item.getItemName().trim().isEmpty()) {
            return "缺少餐點名稱";
        }
        if (item.getQuantity() <= 0) {
            return "數量必須至少 1 份";
        }
        if (item.getUnitPrice() < 0 || item.getOrderTotalPrice() < 0) {
            return "金額不可為負數";
        }
        if (item.getCustomizations() == null) {
            item.setCustomizations(new java.util.ArrayList<>());
        }
        return null;
    }

    private OrderItem findOrderItem(List<OrderItem> orderItems, String itemId) {
        return orderItems.stream()
                .filter(item -> itemId.equals(item.getItemID()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到該餐點"));
    }
}
