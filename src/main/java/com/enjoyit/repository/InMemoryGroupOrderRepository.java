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
    private final Map<String, GroupOrder> orders = new ConcurrentHashMap<>();

    @Override
    public GroupOrder save(GroupOrder order) {
        if (order.getOrderId() == null) {
            order.setOrderId("order_" + System.currentTimeMillis());
        }
        orders.put(order.getOrderId(), order);
        return order;
    }

    @Override
    public Optional<GroupOrder> findById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public List<GroupOrder> findAll() {
        return new ArrayList<>(orders.values());
    }

    @Override
    public List<GroupOrder> findByGroupId(String groupId) {
        return orders.values().stream()
                .filter(order -> groupId.equals(order.getGroupId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByVendorIdAndStatus(String vendorId, String status) {
        return orders.values().stream()
                .anyMatch(order -> vendorId.equals(order.getVendorId()) && status.equals(order.getStatus()));
    }

    @Override
    public void delete(String orderId) {
        orders.remove(orderId);
    }
}
