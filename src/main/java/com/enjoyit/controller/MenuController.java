package com.enjoyit.controller;

import com.enjoyit.domain.Menu;
import com.enjoyit.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendors/{vendorId}/menu")
public class MenuController {
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    // 接收前端傳來的一整包 JSON 菜單草稿
    @PostMapping("/submit")
    public ResponseEntity<String> submitMenuCreation(@PathVariable String vendorId, @RequestBody Menu menu) {
        menuService.submitMenuCreation(vendorId, menu);
        return ResponseEntity.ok("菜單新增成功");
    }

    // 取得該店家的完整菜單
    @GetMapping
    public ResponseEntity<Menu> fetchMenuData(@PathVariable String vendorId) {
        return ResponseEntity.ok(menuService.fetchMenuData(vendorId));
    }

    // 更新單一品項 (使用 PatchMapping 代表部分更新)
    @PatchMapping("/items/{itemId}")
    public ResponseEntity<String> updateMenuItem(
            @PathVariable String vendorId,
            @PathVariable String itemId,
            @RequestParam(required = false) Integer newPrice,
            @RequestParam(required = false) Boolean isActive) {

        menuService.updateMenuItem(vendorId, itemId, newPrice, isActive);
        return ResponseEntity.ok("品項更新成功");
    }
}