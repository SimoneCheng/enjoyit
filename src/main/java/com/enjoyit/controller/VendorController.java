package com.enjoyit.controller;

import com.enjoyit.domain.Vendor;
import com.enjoyit.service.VendorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping
    public List<Vendor> getAllVendors() {
        // 回傳所有啟用的店家，供任一群組選擇
        return vendorService.getAllActiveVendors();
    }

    @PostMapping
    public ResponseEntity<Vendor> createVendor(@RequestBody Map<String, String> request) {
        Vendor vendor = vendorService.createVendor(
            request.get("name"),
            request.get("phone"),
            request.get("address")
        );
        return ResponseEntity.ok(vendor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateVendor(@PathVariable String id, @RequestBody Map<String, String> request) {
        vendorService.updateVendor(
            id,
            request.get("name"),
            request.get("phone"),
            request.get("address"),
            request.get("businessHours")
        );
        return ResponseEntity.ok("店家資訊已更新");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVendor(@PathVariable String id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.ok("店家已成功下架");
    }
}