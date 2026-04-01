package com.enjoyit.service;

import com.enjoyit.domain.Group;
import com.enjoyit.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GroupServiceTest {

    private GroupRepository groupRepository;
    private PasswordEncoder passwordEncoder;
    private GroupService groupService;

    @BeforeEach
    void setUp() {
        groupRepository = Mockito.mock(GroupRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        groupService = new GroupService(groupRepository, passwordEncoder);
    }

    @Test
    void createGroup_Success() {
        // Arrange
        String id = "  testGroup  ";
        String password = "password123";
        String encryptedPassword = "encryptedPassword";
        
        when(groupRepository.findById("testGroup")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encryptedPassword);
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Group result = groupService.createGroup(id, password);

        // Assert
        assertEquals("testGroup", result.getId());
        assertEquals(encryptedPassword, result.getPassword());
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void createGroup_DuplicateId_ThrowsException() {
        // Arrange
        String id = "existingGroup";
        when(groupRepository.findById(id)).thenReturn(Optional.of(new Group(id, "somePass")));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            groupService.createGroup(id, "password123");
        });
    }
}
