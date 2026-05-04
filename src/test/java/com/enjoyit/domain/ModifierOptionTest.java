package com.enjoyit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModifierOptionTest {

    @Test
    @DisplayName("測試帶參數建構子：應自動產生 UUID 並設定名稱與加價金額")
    void testParameterizedConstructor() {
        ModifierOption option = new ModifierOption("加珍珠", 10);

        assertNotNull(option.getId(), "帶參數建立時應自動產生 UUID");
        assertEquals("加珍珠", option.getName());
        assertEquals(10, option.getExtraPrice());
    }

    @Test
    @DisplayName("測試 Setter/Getter：空建構子建立後應能正常修改屬性")
    void testEmptyConstructorAndSetters() {
        ModifierOption option = new ModifierOption(); // 空建構子

        option.setName("半糖");
        option.setExtraPrice(0);

        assertEquals("半糖", option.getName());
        assertEquals(0, option.getExtraPrice());
    }
}