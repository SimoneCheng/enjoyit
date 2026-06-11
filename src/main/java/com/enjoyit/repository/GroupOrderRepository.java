package com.enjoyit.repository;

import com.enjoyit.domain.GroupOrder;
import java.util.Collection;
import java.util.Optional;

public interface GroupOrderRepository {
    void save(GroupOrder order);
    Optional<GroupOrder> findById(String orderId);
    Collection<GroupOrder> findAll();
    void delete(String orderId);
}
