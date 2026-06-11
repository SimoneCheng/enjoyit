package com.enjoyit.repository;

import com.enjoyit.domain.GroupOrder;
import java.util.List;
import java.util.Optional;

public interface GroupOrderRepository {
    GroupOrder save(GroupOrder order);
    Optional<GroupOrder> findById(String id);
    List<GroupOrder> findAll();
    List<GroupOrder> findByGroupId(String groupId);
    boolean existsByVendorIdAndStatus(String vendorId, String status);
}
