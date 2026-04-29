package com.enjoyit.service;

import com.enjoyit.domain.GroupOrder;
import com.enjoyit.domain.Announcement;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class GroupOrderService {

    // 依賴注入範例，雖然目前只用到 PasswordValidator
    private final PasswordValidator passwordValidator;

    public GroupOrderService(PasswordValidator passwordValidator) {
        this.passwordValidator = passwordValidator;
    }

    /**
     * 對應 CO-07: publishGroupOrder
     */
    public GroupOrder publishGroupOrder(String orderInfo, String announcementContent) {
        // 依照 SSD 邏輯建立物件並設定狀態
        GroupOrder order = new GroupOrder(orderInfo);

        if (announcementContent != null && !announcementContent.isEmpty()) {
            order.setAnnouncement(announcementContent);
        }

        order.setStatus("進行中"); // Postcondition: 狀態被設定為「進行中」
        return order;
    }

    /**
     * 對應 CO-08: setOrderDeadline
     */
    public void setOrderDeadline(GroupOrder order, LocalDateTime newTime) {
        // 委託給 Information Expert (GroupOrder) 處理狀態變更邏輯
        if (order != null) {
            order.setOrderDeadline(newTime);
        }
    }

    /**
     * 對應 CO-09: 驗證管理者權限
     */
    public boolean verifyAdminAccess(String inputPassword, String savedPassword) {
        // 使用 Pure Fabrication 的驗證器進行比對
        return passwordValidator.isValid(inputPassword, savedPassword);
    }
}