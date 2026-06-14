package com.enjoyit.service;

import com.enjoyit.domain.Menu;
import com.enjoyit.domain.MenuCategory;
import com.enjoyit.domain.MenuItem;
import com.enjoyit.domain.Vendor;
import com.enjoyit.repository.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private VendorRepository vendorRepository;

    @InjectMocks
    private MenuService menuService;

    private Vendor testVendor;
    private MenuItem testItem;

    @BeforeEach
    void setUp() {
        // 準備測試用的假資料 (Stubbing)
        testVendor = new Vendor("測試店家", "02-55556666", "台北市羅斯福路五段5號");
        testVendor.setId("vendor_001");

        Menu menu = new Menu();
        MenuCategory category = new MenuCategory();
        category.setName("主食");

        testItem = new MenuItem();
        testItem.setId("item_123");
        testItem.setName("排骨飯");
        testItem.setUnitPrice(100);
        testItem.setActive(true);

        category.getItems().add(testItem);
        menu.addCategory(category);
        testVendor.setMenu(menu);
    }

    @Test
    @DisplayName("測試更新餐點：成功更新價格與下架狀態")
    void testUpdateMenuItem_Success() {
        // 告訴 Mock：當有人用 vendor_001 找店家時，回傳我們的假店家
        when(vendorRepository.findById("vendor_001")).thenReturn(Optional.of(testVendor));

        // 執行更新動作：改價為 120，並設為下架 (false)
        menuService.updateMenuItem("vendor_001", "item_123", 120, false);

        // 驗證結果
        assertEquals(120, testItem.getUnitPrice());
        assertFalse(testItem.isActive());
        // 驗證 repository.save 有被呼叫過一次
        verify(vendorRepository, times(1)).save(testVendor);
    }

    @Test
    @DisplayName("測試更新餐點：店家不存在時應拋出 404 例外")
    void testUpdateMenuItem_VendorNotFound() {
        when(vendorRepository.findById("invalid_vendor")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            menuService.updateMenuItem("invalid_vendor", "item_123", 100, true);
        });

        // 驗證如果店家不存在，絕對不能呼叫 save
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("測試更新餐點：餐點 ID 不存在時應拋出 IllegalArgumentException")
    void testUpdateMenuItem_ItemNotFound() {
        when(vendorRepository.findById("vendor_001")).thenReturn(Optional.of(testVendor));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            menuService.updateMenuItem("vendor_001", "wrong_item_id", 100, true);
        });

        assertEquals("找不到該餐點品項", exception.getMessage());
    }

    @Test
    @DisplayName("測試更新餐點：價格小於 0 時應攔截並拋出例外")
    void testUpdateMenuItem_NegativePrice() {
        when(vendorRepository.findById("vendor_001")).thenReturn(Optional.of(testVendor));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            menuService.updateMenuItem("vendor_001", "item_123", -50, true);
        });

        assertEquals("價格不可為負數", exception.getMessage());
    }
}
