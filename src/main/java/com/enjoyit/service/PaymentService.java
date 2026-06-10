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

    public List<Map<String, Object>> getFinanceSummary(String orderId, String password) {
        GroupOrder order = groupOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("找不到該訂單"));

        // 核對主揪管理者密碼
        if (order.getAdminPassword() == null || !order.getAdminPassword().equals(password)) {
            throw new IllegalArgumentException("密碼錯誤，拒絕存取財務資料！");
        }

        return order.getFinanceSummary();
    }

//    public void updatePaymentStatus(String orderId, String participantId, String status) {
//        GroupOrder order = groupOrderRepository.findById(orderId)
//                .orElseThrow(() -> new IllegalArgumentException("找不到該訂單"));
//        order.updatePaymentStatus(participantId, status);
//        groupOrderRepository.save(order); // 狀態變更後存回記憶體 Repository
//    }
    // 【修改】參數增加 String remarks
    public void updatePaymentStatus(String orderId, String participantId, String status, String remarks) {
        GroupOrder order = groupOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("找不到該訂單"));
        order.updatePaymentStatus(participantId, status, remarks); // 傳入 remarks
        groupOrderRepository.save(order);
    }

    public Map<String, Object> getParticipantStatus(String orderId, String participantId) {
        GroupOrder order = groupOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("找不到該訂單"));
        return order.getSingleParticipantStatus(participantId);
    }
}