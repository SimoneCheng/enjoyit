package com.enjoyit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModifierGroupTest {

    @Test
    @DisplayName("測試帶參數建構子：應自動產生 UUID、設定名稱並初始化空列表")
    void testParameterizedConstructor() {
        ModifierGroup group = new ModifierGroup("甜度");

        assertNotNull(group.getId(), "UUID 不應為 null");
        assertEquals("甜度", group.getName());
        assertNotNull(group.getOptions(), "選項列表不應為 null");
        assertTrue(group.getOptions().isEmpty(), "選項列表應為空");
    }

    @Test
    @DisplayName("測試空建構子：應自動產生 UUID 並初始化空列表 (確保反序列化安全)")
    void testEmptyConstructor() {
        ModifierGroup group = new ModifierGroup();

        assertNotNull(group.getId(), "即使是空建構子也應產生 UUID");
        assertNotNull(group.getOptions(), "選項列表不應為 null");
    }

    @Test
    @DisplayName("測試加入選項：addOption 應成功將選項加入列表")
    void testAddOption() {
        ModifierGroup group = new ModifierGroup("冰塊");
        ModifierOption option = new ModifierOption("去冰", 0);

        group.addOption(option);

        assertEquals(1, group.getOptions().size());
        assertEquals("去冰", group.getOptions().get(0).getName());
    }
}