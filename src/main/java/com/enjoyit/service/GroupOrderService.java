package com.enjoyit.service;

import com.enjoyit.domain.GroupOrder;
import com.enjoyit.domain.Announcement;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class GroupOrderService {

    private final PasswordValidator passwordValidator;
    private final com.enjoyit.repository.GroupOrderRepository groupOrderRepository;

    public GroupOrderService(PasswordValidator passwordValidator, com.enjoyit.repository.GroupOrderRepository groupOrderRepository) {
        this.passwordValidator = passwordValidator;
        this.groupOrderRepository = groupOrderRepository;
    }

    /**
     * 對應 CO-07: publishGroupOrder
     */
    public GroupOrder publishGroupOrder(String orderInfo, String announcementContent, String vendorId, String adminPassword, String groupId) {
        GroupOrder order = new GroupOrder(orderInfo);
        order.setVendorId(vendorId);
        order.setAdminPassword(adminPassword);
        order.setGroupId(groupId);

        if (announcementContent != null && !announcementContent.isEmpty()) {
            order.setAnnouncement(announcementContent);
        }

        order.setStatus("進行中");
        return groupOrderRepository.save(order);
    }

    /**
     * 對應 CO-08: setOrderDeadline
     */
    public void setOrderDeadline(GroupOrder order, LocalDateTime newTime) {
        if (order != null) {
            order.setOrderDeadline(newTime);
            groupOrderRepository.save(order);
        }
    }

    /**
     * 檢查特定店家是否有進行中的團購
     */
    public boolean hasOngoingOrdersByVendor(String vendorId) {
        return groupOrderRepository.existsByVendorIdAndStatus(vendorId, "進行中");
    }

    public boolean verifyAdminAccess(String inputPassword, String savedPassword) {
        return passwordValidator.isValid(inputPassword, savedPassword);
    }
}