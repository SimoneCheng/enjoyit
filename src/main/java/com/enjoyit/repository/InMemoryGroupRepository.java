package com.enjoyit.repository;

import com.enjoyit.domain.Group;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGroupRepository implements GroupRepository {
    private final Map<String, Group> groups = new ConcurrentHashMap<>();

    @Override
    public Group save(Group group) {
        groups.put(group.getId(), group);
        return group;
    }

    @Override
    public Optional<Group> findById(String id) {
        return Optional.ofNullable(groups.get(id));
    }
}
