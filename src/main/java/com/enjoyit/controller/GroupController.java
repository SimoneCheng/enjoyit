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
                    "message", "註冊成功"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginGroup(@RequestBody Map<String, String> request) {
        String id = request.get("id");
        String password = request.get("password");

        if (groupService.login(id, password)) {
            return ResponseEntity.ok(Map.of("message", "登入成功", "groupId", id.trim()));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "帳號或密碼錯誤"));
        }
    }
}