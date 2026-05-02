package com.enjoyit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuTest {

    @Test
    @DisplayName("測試 Menu 初始化：預設狀態應為上架 (isActive = true)，且分類清單為空")
    void testMenuInitialization() {
        Menu menu = new Menu();

        // 驗證預設值
        assertTrue(menu.getIsActive(), "預設應該要是上架狀態");
        assertNotNull(menu.getCategories(), "分類清單不應為 null");
        assertTrue(menu.getCategories().isEmpty(), "剛建立時分類清單應為空");
    }

    @Test
    @DisplayName("測試 Menu 狀態切換：應能成功修改 isActive 狀態")
    void testSetIsActive() {
        Menu menu = new Menu();
        menu.setIsActive(false);

        assertFalse(menu.getIsActive(), "狀態應該成功被切換為下架 (false)");
    }

    @Test
    @DisplayName("測試新增分類：addCategory 應成功將分類加入清單")
    void testAddCategory() {
        Menu menu = new Menu();
        MenuCategory category = new MenuCategory();
        category.setName("飲料區");

        menu.addCategory(category);

        assertEquals(1, menu.getCategories().size(), "清單內應該要有 1 個分類");
        assertEquals("飲料區", menu.getCategories().get(0).getName());
    }

    @Test
    @DisplayName("測試設定分類清單：setCategories 應能直接替換整個清單")
    void testSetCategories() {
        Menu menu = new Menu();
        List<MenuCategory> newCategories = new ArrayList<>();
        newCategories.add(new MenuCategory());
        newCategories.add(new MenuCategory());

        menu.setCategories(newCategories);

        assertEquals(2, menu.getCategories().size(), "清單應該被成功替換為包含 2 個分類的新清單");
    }
}