package com.enjoyit.repository;

import com.enjoyit.domain.Group;
import java.util.Optional;

public interface GroupRepository {
    Group save(Group group);
    Optional<Group> findById(String id);
}
