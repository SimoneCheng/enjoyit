package com.enjoyit.controller;

import com.enjoyit.domain.Group;
import com.enjoyit.dto.GroupCreateRequest;
import com.enjoyit.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerGroup(@Valid @RequestBody GroupCreateRequest request) {
        try {
            Group group = groupService.createGroup(request.getId(), request.getPassword());
            return ResponseEntity.ok(Map.of(
                "message", "註冊成功",
                "groupId", group.getId(),
                "shareUrl", "/login?groupId=" + group.getId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
