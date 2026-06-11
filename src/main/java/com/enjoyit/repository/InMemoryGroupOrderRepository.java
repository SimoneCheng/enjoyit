package com.enjoyit.repository;

import com.enjoyit.domain.GroupOrder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryGroupOrderRepository implements GroupOrderRepository {
    private final Map<String, GroupOrder> ordersMap = new ConcurrentHashMap<>();

    @Override
    public GroupOrder save(GroupOrder order) {
        if (order.getOrderId() == null) {
            order.setOrderId("order_" + System.currentTimeMillis());
        }
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

    @Override
    public List<GroupOrder> findByGroupId(String groupId) {
        return ordersMap.values().stream()
                .filter(order -> groupId.equals(order.getGroupId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByVendorIdAndStatus(String vendorId, String status) {
        return ordersMap.values().stream()
                .anyMatch(order -> vendorId.equals(order.getVendorId()) && status.equals(order.getStatus()));
    }
}
