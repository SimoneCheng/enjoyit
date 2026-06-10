package com.enjoyit.repository;

import com.enjoyit.domain.GroupOrder;
import java.util.Optional;
import java.util.List;

public interface GroupOrderRepository {
    GroupOrder save(GroupOrder order);
    Optional<GroupOrder> findById(String id);
    List<GroupOrder> findAll();
}