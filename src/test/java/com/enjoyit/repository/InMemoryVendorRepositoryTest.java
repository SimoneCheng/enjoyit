package com.enjoyit.repository;

import com.enjoyit.domain.Vendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryVendorRepositoryTest {

    private InMemoryVendorRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryVendorRepository();
        // 手動呼叫初始化方法，模擬 Spring Boot 啟動時的 @PostConstruct 行為
        repository.seedTestVendor();
    }

    @Test
    @DisplayName("測試 Repository 初始化：應自動包含預設的 vendor_001")
    void testSeedTestVendor() {
        Optional<Vendor> vendor = repository.findById("vendor_001");
        assertTrue(vendor.isPresent(), "應該要找到預設的 vendor_001");
        assertEquals("測試店家", vendor.get().getName());
    }

    @Test
    @DisplayName("測試儲存店家：儲存後應能透過 ID 成功找回，且總數增加")
    void testSaveAndFindById() {
        Vendor newVendor = new Vendor("大苑子");
        newVendor.setId("vendor_002");

        repository.save(newVendor);

        Optional<Vendor> foundVendor = repository.findById("vendor_002");
        assertTrue(foundVendor.isPresent());
        assertEquals("大苑子", foundVendor.get().getName());

        List<Vendor> allVendors = repository.findAll();
        assertEquals(2, allVendors.size(), "資料庫中應該要有 2 筆店家資料");
    }
}