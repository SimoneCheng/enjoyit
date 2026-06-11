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
    public List<Vendor> getVendors(@RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        if (activeOnly) {
            return vendorService.getAllActiveVendors();
        }
        return vendorService.getAllVendors();
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
            request.get("address")
        );
        return ResponseEntity.ok("店家資訊已更新");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVendor(@PathVariable String id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.ok("店家已成功下架");
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<String> setVendorStatus(@PathVariable String id, @RequestParam boolean active) {
        vendorService.setVendorActiveStatus(id, active);
        return ResponseEntity.ok(active ? "店家已成功上架" : "店家已成功下架");
    }
    }