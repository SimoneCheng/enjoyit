package com.enjoyit.controller;

import com.enjoyit.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group-orders/{orderId}/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // 主揪獲取所有人訂購項目與付款狀態表格 (帶入密碼驗證)
    @GetMapping("/summary")
    public ResponseEntity<?> getFinanceSummary(
            @PathVariable String orderId,
            @RequestParam String password) {
        try {
            List<Map<String, Object>> summary = paymentService.getFinanceSummary(orderId, password);
            return ResponseEntity.ok(summary);
        } catch (IllegalArgumentException e) {
            // 密碼錯誤或找不到訂單時，回傳 400 錯誤訊息
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

//    // 主揪更新某個人的付款狀態
//    @PutMapping("/status")
//    public ResponseEntity<String> updatePaymentStatus(
//            @PathVariable String orderId,
//            @RequestParam String participantId,
//            @RequestParam String status) {
//        try {
//            paymentService.updatePaymentStatus(orderId, participantId, status);
//            return ResponseEntity.ok("狀態更新成功");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
    // 主揪更新某個人的付款狀態與備註
    @PutMapping("/status")
    public ResponseEntity<String> updatePaymentStatus(
            @PathVariable String orderId,
            @RequestParam String participantId,
            @RequestParam String status,
            @RequestParam(required = false) String remarks) { // 【新增】接收備註參數
        try {
            paymentService.updatePaymentStatus(orderId, participantId, status, remarks != null ? remarks : "");
            return ResponseEntity.ok("狀態與備註更新成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 個人裝置查詢自己的付款金額與狀態
    @GetMapping("/my-status")
    public ResponseEntity<Map<String, Object>> getMyStatus(
            @PathVariable String orderId,
            @RequestParam String participantId) {
        return ResponseEntity.ok(paymentService.getParticipantStatus(orderId, participantId));
    }
}