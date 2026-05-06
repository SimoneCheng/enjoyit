package com.enjoyit.service;

import com.enjoyit.domain.GroupOrder;
import com.enjoyit.domain.OrderItem;
import com.enjoyit.repository.GroupOrderRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupOrderService {

    private final PasswordValidator passwordValidator;
    private final GroupOrderRepository groupOrderRepository;

    public GroupOrderService(PasswordValidator passwordValidator, GroupOrderRepository groupOrderRepository) {
        this.passwordValidator = passwordValidator;
        this.groupOrderRepository = groupOrderRepository;
    }

    /**
     * 對應 CO-07: publishGroupOrder
     */
    public String publishGroupOrder(String orderInfo, String announcementContent, String vendorId, String adminPassword, String groupId) {
        GroupOrder order = new GroupOrder(orderInfo);
        String orderId = "order_" + System.currentTimeMillis();
        order.setOrderId(orderId);
        order.setVendorId(vendorId);
        order.setAdminPassword(adminPassword);
        order.setGroupId(groupId.trim());

        if (announcementContent != null && !announcementContent.isEmpty()) {
            order.setAnnouncement(announcementContent);
        }

        order.setStatus("進行中");
        groupOrderRepository.save(order);
        return orderId;
    }

    public Optional<GroupOrder> getOrderById(String orderId) {
        return groupOrderRepository.findById(orderId);
    }

    public Collection<GroupOrder> getOrdersByGroupId(String groupId) {
        return groupOrderRepository.findAll().stream()
                .filter(order -> groupId.equals(order.getGroupId()))
                .collect(Collectors.toList());
    }

    public void addOrderItem(GroupOrder order, OrderItem item) {
        order.getOrderItems().add(item);
        groupOrderRepository.save(order);
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

    public void closeOrder(GroupOrder order) {
        if (order != null) {
            order.setStatus("已結單");
            groupOrderRepository.save(order);
        }
    }

    /**
     * 對應 CO-09: 驗證管理者權限
     */
    public boolean verifyAdminAccess(String inputPassword, String savedPassword) {
        return passwordValidator.isValid(inputPassword, savedPassword);
    }
}
