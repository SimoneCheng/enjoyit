package com.enjoyit.service;

import com.enjoyit.domain.GroupOrder;
import com.enjoyit.repository.GroupOrderRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private final GroupOrderRepository groupOrderRepository;

    public PaymentService(GroupOrderRepository groupOrderRepository) {
        this.groupOrderRepository = groupOrderRepository;
    }

    // 【修改】移除密碼檢查，開放所有人調用總表
    public List<Map<String, Object>> getFinanceSummary(String orderId) {
        GroupOrder order = groupOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("找不到該訂單"));
        return order.getFinanceSummary();
    }

    // 【修改】將密碼檢查移到這裡，只有主揪能改狀態
    public void updatePaymentStatus(String orderId, String payerName, String status, String remarks, String password) {
        GroupOrder order = groupOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("找不到該訂單"));

        // 核對主揪管理者密碼
        if (order.getAdminPassword() == null || !order.getAdminPassword().equals(password)) {
            throw new IllegalArgumentException("密碼錯誤，拒絕修改財務狀態！");
        }

        // 先同步產出最新帳單，避免尚未查過 summary 時找不到付款紀錄
        order.getFinanceSummary();
        order.updatePaymentStatus(payerName, status, remarks);
        groupOrderRepository.save(order);
    }
}
