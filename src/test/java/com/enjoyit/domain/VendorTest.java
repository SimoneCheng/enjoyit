package com.enjoyit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VendorTest {

    @Test
    @DisplayName("測試建立店家：應自動產生 UUID、設定名稱，並預設帶有一張空菜單")
    void testVendorCreation() {
        Vendor vendor = new Vendor("五十嵐");

        assertNotNull(vendor.getId(), "應自動產生店家 UUID");
        assertEquals("五十嵐", vendor.getName());

        // 驗證預設行為：菜單不應為 null
        assertNotNull(vendor.getMenu(), "店家建立時必須自帶一張預設空菜單");
        assertTrue(vendor.getMenu().getCategories().isEmpty(), "預設菜單內的分類應該是空的");
    }

    @Test
    @DisplayName("測試屬性修改：應能重新設定店家 ID 與替換整張菜單")
    void testSetters() {
        Vendor vendor = new Vendor("可不可");
        vendor.setId("custom_vendor_999");

        Menu newMenu = new Menu();
        newMenu.setIsActive(false);
        vendor.setMenu(newMenu);

        assertEquals("custom_vendor_999", vendor.getId());
        assertFalse(vendor.getMenu().getIsActive(), "菜單應該成功被替換為我們新設定的菜單");
    }
}