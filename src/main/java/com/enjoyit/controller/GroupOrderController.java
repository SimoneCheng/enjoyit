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

    /**
     * CO-07: publishGroupOrder(orderInfo, announcement)
     * 建立新的團購活動並發布公告
     */
    @PostMapping("/publish")
    public ResponseEntity<?> publishGroupOrder(@RequestBody Map<String, String> request) {
        String orderInfo = request.get("orderInfo");
        String announcement = request.get("announcement");

        // 建立實例並設定初始狀態
        currentGroupOrder = new GroupOrder(orderInfo);
        if (announcement != null && !announcement.isEmpty()) {
            currentGroupOrder.setAnnouncement(announcement);
        }

        currentGroupOrder.setStatus("進行中");
        return ResponseEntity.ok("displayOrderLink");
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
                return ResponseEntity.ok("已設定截止時間：" + newTime.toString());
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