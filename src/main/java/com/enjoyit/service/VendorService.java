package com.enjoyit.service;

import com.enjoyit.domain.Vendor;
import com.enjoyit.repository.VendorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;
    private final GroupOrderService groupOrderService;

    public VendorService(VendorRepository vendorRepository, GroupOrderService groupOrderService) {
        this.vendorRepository = vendorRepository;
        this.groupOrderService = groupOrderService;
    }

    public List<Vendor> getAllActiveVendors() {
        return vendorRepository.findAllActive();
    }

    public Vendor createVendor(String name, String phone, String address) {
        validateVendorFields(name, phone, address);
        
        if (vendorRepository.existsByNameAndAddress(name, address)) {
            throw new IllegalStateException("店家資訊重複，新增失敗");
        }

        Vendor vendor = new Vendor(name, phone, address);
        vendorRepository.save(vendor);
        return vendor;
    }

    public void updateVendor(String id, String name, String phone, String address, String businessHours) {
        Vendor vendor = vendorRepository.findById(id).orElse(null);
        if (vendor == null) {
            throw new IllegalArgumentException("找不到店家");
        }

        validateVendorFields(name, phone, address);
        
        vendor.setName(name);
        vendor.setPhone(phone);
        vendor.setAddress(address);
        vendor.setBusinessHours(businessHours);
        
        vendorRepository.save(vendor);
    }

    public void deleteVendor(String id) {
        if (groupOrderService.hasOngoingOrdersByVendor(id)) {
            throw new IllegalStateException("有團購在使用此店家，不得下架或刪除");
        }

        Vendor vendor = vendorRepository.findById(id).orElse(null);
        if (vendor == null) {
            throw new IllegalArgumentException("找不到店家");
        }
        
        vendor.setActive(false);
        vendorRepository.save(vendor);
    }

    private void validateVendorFields(String name, String phone, String address) {
        if (name == null || name.isBlank() || 
            phone == null || phone.isBlank() || 
            address == null || address.isBlank()) {
            throw new IllegalArgumentException("必填欄位（店家名稱、電話、地址）尚未填寫");
        }
    }
}