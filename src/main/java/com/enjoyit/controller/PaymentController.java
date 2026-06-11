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

    // 所有人皆可獲取對帳表 (移除了 @RequestParam String password)
    @GetMapping("/summary")
    public ResponseEntity<?> getFinanceSummary(@PathVariable String orderId) {
        try {
            return ResponseEntity.ok(paymentService.getFinanceSummary(orderId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 主揪更新某個人的付款狀態與備註 (增加了 @RequestParam String password)
    @PutMapping("/status")
    public ResponseEntity<String> updatePaymentStatus(
            @PathVariable String orderId,
            @RequestParam String payerName,
            @RequestParam String status,
            @RequestParam(required = false) String remarks,
            @RequestParam String password) {
        try {
            paymentService.updatePaymentStatus(orderId, payerName, status, remarks != null ? remarks : "", password);
            return ResponseEntity.ok("狀態與備註更新成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}