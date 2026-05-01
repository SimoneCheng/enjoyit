package com.enjoyit.service;

import com.enjoyit.domain.Menu;
import com.enjoyit.domain.MenuItem;
import com.enjoyit.domain.Vendor;
import com.enjoyit.repository.VendorRepository;
import org.springframework.stereotype.Service;

@Service
public class MenuService {
    private final VendorRepository vendorRepository;

    public MenuService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    // 對應 Operation Contract: submitMenuCreation
    public void submitMenuCreation(String vendorId, Menu menu) {
        // 如果找不到該店家，就自動幫他建一個 (解決預設資料為空的問題)
        Vendor vendor = vendorRepository.findById(vendorId).orElseGet(() -> {
            Vendor newVendor = new Vendor("測試店家");
            newVendor.setId(vendorId); // 強制設為我們前端傳來的 vendor_001
            return newVendor;
        });

        vendor.setMenu(menu);

        vendorRepository.save(vendor);
    }

    // 對應 Operation Contract: fetchMenuData
    public Menu fetchMenuData(String vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("找不到該店家"));
        return vendor.getMenu();
    }

    // 對應 Operation Contract: updateMenuItem (處理改價與上下架)
    public void updateMenuItem(String vendorId, String itemId, Integer newPrice, Boolean isActive) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("找不到該店家"));

        // 在菜單中尋找對應的 MenuItem
        MenuItem targetItem = null;
        for (var category : vendor.getMenu().getCategories()) {
            for (var item : category.getItems()) {
                if (item.getId().equals(itemId)) {
                    targetItem = item;
                    break;
                }
            }
        }

        if (targetItem == null) {
            throw new IllegalArgumentException("找不到該餐點品項");
        }

        // 邏輯驗證：價格不可為負數
        if (newPrice != null) {
            if (newPrice < 0) {
                throw new IllegalArgumentException("價格不可為負數");
            }
            targetItem.setUnitPrice(newPrice);
        }

        // 邏輯驗證：上下架狀態更新 (Soft Delete 機制)
        if (isActive != null) {
            targetItem.setActive(isActive);
        }

        vendorRepository.save(vendor);
    }
}