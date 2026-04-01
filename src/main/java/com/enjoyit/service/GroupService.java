package com.enjoyit.service;

import com.enjoyit.domain.Group;
import com.enjoyit.repository.GroupRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;

    public GroupService(GroupRepository groupRepository, PasswordEncoder passwordEncoder) {
        this.groupRepository = groupRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Group createGroup(String id, String rawPassword) {
        String trimmedId = id.trim();
        if (groupRepository.findById(trimmedId).isPresent()) {
            throw new IllegalArgumentException("此帳號已存在，請使用其他名稱");
        }
        
        String encryptedPassword = passwordEncoder.encode(rawPassword.trim());
        Group group = new Group(trimmedId, encryptedPassword);
        return groupRepository.save(group);
    }
}
