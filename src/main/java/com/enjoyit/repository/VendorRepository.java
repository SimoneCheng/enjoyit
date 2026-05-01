package com.enjoyit.repository;

import com.enjoyit.domain.Vendor;
import java.util.Optional;
import java.util.List;

public interface VendorRepository {
    void save(Vendor vendor);
    Optional<Vendor> findById(String id);
    List<Vendor> findAll();
}