package com.enjoyit.controller;

import com.enjoyit.domain.GroupOrder;
import com.enjoyit.dto.OrderItemsBatchRequest;
import com.enjoyit.dto.DeadlineRequest;
import com.enjoyit.service.OrderSummaryGenerator;
import com.enjoyit.service.PasswordValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/group-orders")
public class GroupOrderController {

    private final com.enjoyit.service.GroupOrderService groupOrderService;
    private final com.enjoyit.repository.GroupOrderRepository groupOrderRepository;
    private final PasswordValidator passwordValidator = new PasswordValidator();

    public GroupOrderController(com.enjoyit.service.GroupOrderService groupOrderService, 
                               com.enjoyit.repository.GroupOrderRepository groupOrderRepository) {
        this.groupOrderService = groupOrderService;
        this.groupOrderRepository = groupOrderRepository;
    }

    /**
     * CO-07: publishGroupOrder(orderInfo, announcement)
     * 建立新的團購活動並發布公告
     */
    @PostMapping("/publish")
    public ResponseEntity<?> publishGroupOrder(@RequestBody Map<String, String> request) {
        String orderInfo = request.get("orderInfo");
        String announcement = request.get("announcement");
        String vendorId = request.get("vendorId");
        String adminPassword = request.get("adminPassword");
        String groupId = request.get("groupId");

        if (groupId == null || groupId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("缺少群組資訊");
        }

        GroupOrder newOrder = groupOrderService.publishGroupOrder(orderInfo, announcement, vendorId, adminPassword, groupId.trim());
        return ResponseEntity.ok(newOrder.getOrderId());
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
            @RequestBody com.enjoyit.domain.OrderItem item) {
        GroupOrder order = requireOrder(orderId, groupId);
        String closedMessage = getClosedOrderMessage(order);
        if (closedMessage != null) {
            return ResponseEntity.badRequest().body(closedMessage);
        }
        String validationMessage = validateOrderItem(item);
        if (validationMessage != null) {
            return ResponseEntity.badRequest().body(validationMessage);
        }

        order.getOrderItems().add(item);
        groupOrderRepository.save(order);
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

        for (com.enjoyit.domain.OrderItem item : request.getItems()) {
            String validationMessage = validateOrderItem(item);
            if (validationMessage != null) {
                return ResponseEntity.badRequest().body(validationMessage);
            }
        }

        order.getOrderItems().addAll(request.getItems());
        groupOrderRepository.save(order);
        return ResponseEntity.ok(request.getItems());
    }

    @PutMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<?> updateOrderItem(
            @PathVariable String orderId,
            @PathVariable String itemId,
            @RequestParam String groupId,
            @RequestBody com.enjoyit.domain.OrderItem updatedItem) {
        GroupOrder order = requireOrder(orderId, groupId);
        String closedMessage = getClosedOrderMessage(order);
        if (closedMessage != null) {
            return ResponseEntity.badRequest().body(closedMessage);
        }
        String validationMessage = validateOrderItem(updatedItem);
        if (validationMessage != null) {
            return ResponseEntity.badRequest().body(validationMessage);
        }

        com.enjoyit.domain.OrderItem existingItem = findOrderItem(order.getOrderItems(), itemId);
        updatedItem.setParticipantId(updatedItem.getParticipantId().trim());
        updatedItem.setOrderFor(updatedItem.getOrderFor().trim());
        updatedItem.setItemName(updatedItem.getItemName().trim());

        existingItem.setParticipantId(updatedItem.getParticipantId());
        existingItem.setOrderFor(updatedItem.getOrderFor());
        existingItem.setMenuItemId(updatedItem.getMenuItemId());
        existingItem.setItemName(updatedItem.getItemName());
        existingItem.setUnitPrice(updatedItem.getUnitPrice());
        existingItem.setCustomizations(updatedItem.getCustomizations());
        existingItem.setQuantity(updatedItem.getQuantity());
        existingItem.setOrderTotalPrice(updatedItem.getOrderTotalPrice());
        
        groupOrderRepository.save(order);
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

        com.enjoyit.domain.OrderItem existingItem = findOrderItem(order.getOrderItems(), itemId);
        order.getOrderItems().remove(existingItem);
        groupOrderRepository.save(order);
        return ResponseEntity.ok("餐點已取消");
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders(@RequestParam String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("缺少群組資訊");
        }
        return ResponseEntity.ok(groupOrderRepository.findByGroupId(groupId));
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
    public ResponseEntity<?> closeOrder(
            @PathVariable String orderId,
            @RequestParam String groupId,
            @RequestBody(required = false) Map<String, String> request) {
        GroupOrder order = requireOrder(orderId, groupId);
        String password = request == null ? null : request.get("password");
        if (!groupOrderService.verifyAdminAccess(password, order.getAdminPassword())) {
            return ResponseEntity.status(401).body("密碼錯誤，權限不足");
        }
        order.setStatus("已結單");
        groupOrderRepository.save(order);
        return ResponseEntity.ok("訂單已成功結單");
    }

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
        GroupOrder order = groupOrderRepository.findById(orderId).orElse(null);
        if (order == null || !groupId.equals(order.getGroupId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到該訂單");
        }
        return order;
    }

    private String getClosedOrderMessage(GroupOrder order) {
        if ("已結單".equals(order.getStatus())) {
            return "此團購已截止，無法點餐";
        }
        return null;
    }

    private String validateOrderItem(com.enjoyit.domain.OrderItem item) {
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

    private com.enjoyit.domain.OrderItem findOrderItem(List<com.enjoyit.domain.OrderItem> orderItems, String itemId) {
        return orderItems.stream()
                .filter(item -> itemId.equals(item.getItemID()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到該餐點"));
    }
}
