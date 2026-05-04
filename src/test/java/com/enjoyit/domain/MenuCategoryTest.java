package com.enjoyit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenuCategoryTest {

    @Test
    @DisplayName("測試帶參數建構子：應自動產生 UUID、設定名稱並初始化空列表")
    void testParameterizedConstructor() {
        MenuCategory category = new MenuCategory("主餐區");

        assertNotNull(category.getId());
        assertEquals("主餐區", category.getName());
        assertNotNull(category.getItems());
        assertTrue(category.getItems().isEmpty());
    }

    @Test
    @DisplayName("測試空建構子：應自動產生 UUID 並初始化空列表")
    void testEmptyConstructor() {
        MenuCategory category = new MenuCategory();

        assertNotNull(category.getId());
        assertNotNull(category.getItems());
    }

    @Test
    @DisplayName("測試加入品項：addItem 應成功將品項加入列表")
    void testAddItem() {
        MenuCategory category = new MenuCategory("飲料");
        MenuItem item = new MenuItem("拿鐵", 60);

        category.addItem(item);

        assertEquals(1, category.getItems().size());
        assertEquals("拿鐵", category.getItems().get(0).getName());
    }
}