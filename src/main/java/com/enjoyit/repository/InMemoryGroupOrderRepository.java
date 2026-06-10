package com.enjoyit.repository;

import com.enjoyit.domain.GroupOrder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGroupOrderRepository implements GroupOrderRepository {
    // 將原本存在 Controller 的 ordersMap 移到這裡統一管理
    private final Map<String, GroupOrder> ordersMap = new ConcurrentHashMap<>();

    @Override
    public GroupOrder save(GroupOrder order) {
        ordersMap.put(order.getOrderId(), order);
        return order;
    }

    @Override
    public Optional<GroupOrder> findById(String id) {
        return Optional.ofNullable(ordersMap.get(id));
    }

    @Override
    public List<GroupOrder> findAll() {
        return new ArrayList<>(ordersMap.values());
    }
}