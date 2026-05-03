package com.enjoyit.service;

import com.enjoyit.domain.Menu;
import com.enjoyit.domain.MenuCategory;
import com.enjoyit.domain.MenuItem;
import com.enjoyit.domain.Vendor;
import com.enjoyit.repository.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuServiceTest {

    private VendorRepository vendorRepository;
    private MenuService menuService;

    @BeforeEach
    void setUp() {
        vendorRepository = mock(VendorRepository.class);
        menuService = new MenuService(vendorRepository);
    }

    @Test
    @DisplayName("UC-03: 新增菜單 - 成功將菜單寫入資料結構")
    void submitMenuCreation_Success() {
        String vendorId = "v1";
        Vendor vendor = new Vendor("店家A");
        vendor.setId(vendorId);
        Menu menu = new Menu();

        when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor));

        menuService.submitMenuCreation(vendorId, menu);

        assertEquals(menu, vendor.getMenu());
        verify(vendorRepository, times(1)).save(vendor);
    }

    @Test
    @DisplayName("UC-03: 修改餐點價格 - 成功更新價格")
    void updateMenuItem_PriceUpdate_Success() {
        String vendorId = "v1";
        Vendor vendor = new Vendor("店家A");
        vendor.setId(vendorId);
        
        Menu menu = new Menu();
        MenuCategory category = new MenuCategory("主食");
        MenuItem item = new MenuItem("排骨飯", 100);
        String itemId = item.getId();
        category.addItem(item);
        menu.addCategory(category);
        vendor.setMenu(menu);

        when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor));

        menuService.updateMenuItem(vendorId, itemId, 120, null);

        assertEquals(120, item.getUnitPrice());
        verify(vendorRepository, times(1)).save(vendor);
    }

    @Test
    @DisplayName("UC-03: 修改餐點狀態 - 成功切換為下架")
    void updateMenuItem_StatusUpdate_Success() {
        String vendorId = "v1";
        Vendor vendor = new Vendor("店家A");
        vendor.setId(vendorId);
        
        Menu menu = new Menu();
        MenuCategory category = new MenuCategory("主食");
        MenuItem item = new MenuItem("排骨飯", 100);
        String itemId = item.getId();
        category.addItem(item);
        menu.addCategory(category);
        vendor.setMenu(menu);

        when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor));

        menuService.updateMenuItem(vendorId, itemId, null, false);

        assertFalse(item.isActive());
        verify(vendorRepository, times(1)).save(vendor);
    }

    @Test
    @DisplayName("UC-03: 修改餐點驗證 - 價格不可為負數")
    void updateMenuItem_NegativePrice_ThrowsException() {
        String vendorId = "v1";
        Vendor vendor = new Vendor("店家A");
        vendor.setId(vendorId);
        
        Menu menu = new Menu();
        MenuCategory category = new MenuCategory("主食");
        MenuItem item = new MenuItem("排骨飯", 100);
        String itemId = item.getId();
        category.addItem(item);
        menu.addCategory(category);
        vendor.setMenu(menu);

        when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor));

        assertThrows(IllegalArgumentException.class, () -> 
            menuService.updateMenuItem(vendorId, itemId, -50, null)
        );
    }

    @Test
    @DisplayName("UC-03: 修改餐點驗證 - 找不到該品項時拋出異常")
    void updateMenuItem_ItemNotFound_ThrowsException() {
        String vendorId = "v1";
        Vendor vendor = new Vendor("店家A");
        vendor.setId(vendorId);
        
        Menu menu = new Menu();
        MenuCategory category = new MenuCategory("主食");
        MenuItem item = new MenuItem("排骨飯", 100);
        category.addItem(item);
        menu.addCategory(category);
        vendor.setMenu(menu);

        when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor));

        assertThrows(IllegalArgumentException.class, () -> 
            menuService.updateMenuItem(vendorId, "non-existent-id", 120, null)
        );
    }
}
