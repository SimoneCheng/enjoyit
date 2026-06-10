package com.enjoyit.repository;

import com.enjoyit.domain.Vendor;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

@Repository
public class InMemoryVendorRepository implements VendorRepository {
    // 使用 ConcurrentHashMap 確保多人同時點餐或操作時的執行緒安全
    private final Map<String, Vendor> store = new ConcurrentHashMap<>();

    @PostConstruct
    public void seedTestVendor() {
        Vendor vendor = new Vendor("測試店家", "02-12345678", "台北市忠孝東路一段1號");
        vendor.setId("vendor_001");
        store.putIfAbsent(vendor.getId(), vendor);
    }

    @Override
    public void save(Vendor vendor) {
        store.put(vendor.getId(), vendor);
    }

    @Override
    public Optional<Vendor> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Vendor> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Vendor> findAllActive() {
        return store.values().stream()
                .filter(Vendor::isActive)
                .toList();
    }

    @Override
    public boolean existsByNameAndAddress(String name, String address) {
        return store.values().stream()
                .anyMatch(v -> v.getName().equals(name) && v.getAddress().equals(address));
    }
}