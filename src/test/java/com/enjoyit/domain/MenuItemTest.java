package com.enjoyit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenuItemTest {

    @Test
    @DisplayName("測試帶參數建構子：應自動產生 UUID、設定名稱、價格，預設上架並初始化客製化列表")
    void testParameterizedConstructor() {
        MenuItem item = new MenuItem("排骨飯", 100);

        assertNotNull(item.getId());
        assertEquals("排骨飯", item.getName());
        assertEquals(100, item.getUnitPrice());
        assertTrue(item.isActive(), "預設狀態應為上架 (true)");
        assertNotNull(item.getModifierGroups());
        assertTrue(item.getModifierGroups().isEmpty());
    }

    @Test
    @DisplayName("測試空建構子：應自動產生 UUID 並初始化客製化列表")
    void testEmptyConstructor() {
        MenuItem item = new MenuItem();

        assertNotNull(item.getId());
        assertNotNull(item.getModifierGroups());
    }

    @Test
    @DisplayName("測試設定與切換狀態：setActive 應能正確修改狀態")
    void testSetActive() {
        MenuItem item = new MenuItem("雞腿飯", 120);
        assertTrue(item.isActive());

        item.setActive(false);
        assertFalse(item.isActive());
    }

    @Test
    @DisplayName("測試加入客製化群組：addModifierGroup 應成功將群組加入列表")
    void testAddModifierGroup() {
        MenuItem item = new MenuItem("珍珠奶茶", 50);
        ModifierGroup group = new ModifierGroup("甜度");

        item.addModifierGroup(group);

        assertEquals(1, item.getModifierGroups().size());
        assertEquals("甜度", item.getModifierGroups().get(0).getName());
    }
}