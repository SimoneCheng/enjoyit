package com.enjoyit.repository;

import com.enjoyit.domain.GroupOrder;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGroupOrderRepository implements GroupOrderRepository {
    private final Map<String, GroupOrder> orders = new ConcurrentHashMap<>();

    @Override
    public void save(GroupOrder order) {
        orders.put(order.getOrderId(), order);
    }

    @Override
    public Optional<GroupOrder> findById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public Collection<GroupOrder> findAll() {
        return orders.values();
    }

    @Override
    public void delete(String orderId) {
        orders.remove(orderId);
    }
}
