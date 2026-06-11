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
    private final Map<String, GroupOrder> orders = new ConcurrentHashMap<>();

    @Override
    public GroupOrder save(GroupOrder order) {
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
    public void delete(String orderId) {
        orders.remove(orderId);
    }
}
