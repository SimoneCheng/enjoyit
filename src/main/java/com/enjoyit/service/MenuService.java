package com.enjoyit.service;

import com.enjoyit.domain.Menu;
import com.enjoyit.domain.MenuItem;
import com.enjoyit.domain.Vendor;
import com.enjoyit.repository.VendorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MenuService {
    private final VendorRepository vendorRepository;

    public MenuService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    // 對應 Operation Contract: submitMenuCreation
    public void submitMenuCreation(String vendorId, Menu menu) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到該店家"));

        vendor.setMenu(menu);

        vendorRepository.save(vendor);
    }

    // 對應 Operation Contract: fetchMenuData
    public Menu fetchMenuData(String vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到該店家"));
        return vendor.getMenu();
    }

    // 對應 Operation Contract: updateMenuItem (處理改價與上下架)
    public void updateMenuItem(String vendorId, String itemId, Integer newPrice, Boolean isActive) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到該店家"));

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