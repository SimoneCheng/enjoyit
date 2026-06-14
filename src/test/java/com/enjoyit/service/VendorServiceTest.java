package com.enjoyit.service;

import com.enjoyit.domain.Vendor;
import com.enjoyit.repository.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VendorServiceTest {

    private VendorRepository vendorRepository;
    private GroupOrderService groupOrderService;
    private VendorService vendorService;

    @BeforeEach
    void setUp() {
        vendorRepository = Mockito.mock(VendorRepository.class);
        groupOrderService = Mockito.mock(GroupOrderService.class);
        vendorService = new VendorService(vendorRepository, groupOrderService);
    }

    @Test
    @DisplayName("建立店家：資料完整應成功儲存")
    void testCreateVendorSuccess() {
        when(vendorRepository.existsByNameAndAddress(anyString(), anyString())).thenReturn(false);

        Vendor vendor = vendorService.createVendor("手搖飲", "02-123", "地址X");

        assertNotNull(vendor);
        assertEquals("手搖飲", vendor.getName());
        verify(vendorRepository, times(1)).save(any(Vendor.class));
    }

    @Test
    @DisplayName("建立店家：必填欄位缺失應拋出異常")
    void testCreateVendorMissingFields() {
        assertThrows(IllegalArgumentException.class, () -> vendorService.createVendor("", "02-123", "地址X"));
        assertThrows(IllegalArgumentException.class, () -> vendorService.createVendor("名", null, "地址X"));
    }

    @Test
    @DisplayName("建立店家：重複店家應拋出異常")
    void testCreateVendorDuplicate() {
        when(vendorRepository.existsByNameAndAddress("舊店家", "舊地址")).thenReturn(true);
        
        assertThrows(IllegalStateException.class, () -> vendorService.createVendor("舊店家", "099", "舊地址"));
    }

    @Test
    @DisplayName("刪除店家：若有進行中訂單應禁止刪除")
    void testDeleteVendorWithOngoingOrders() {
        String vendorId = "v123";
        // 模擬 GroupOrderService 回報該店家有進行中的訂單
        when(groupOrderService.hasOngoingOrdersByVendor(vendorId)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> vendorService.deleteVendor(vendorId));
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("刪除店家：無進行中訂單應成功執行軟刪除")
    void testDeleteVendorSuccess() {
        String vendorId = "v123";
        Vendor vendor = new Vendor("名", "電", "地");
        when(vendorRepository.findById(vendorId)).thenReturn(java.util.Optional.of(vendor));
        when(groupOrderService.hasOngoingOrdersByVendor(vendorId)).thenReturn(false);

        vendorService.deleteVendor(vendorId);

        assertFalse(vendor.isActive(), "店家狀態應變更為下架 (false)");
        verify(vendorRepository, times(1)).save(vendor);
    }
}