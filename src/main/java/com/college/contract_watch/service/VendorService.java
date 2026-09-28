package com.college.contract_watch.service;

import java.util.List;

import com.college.contract_watch.entity.Vendor;
import com.college.contract_watch.exception.VendorNotFoundException;
import com.college.contract_watch.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;

    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public Vendor createVendor(Vendor vendor) {
        vendor.setId(null);
        return vendorRepository.save(vendor);
    }

    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    public Vendor getVendorById(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new VendorNotFoundException(id));
    }

    @Transactional
    public Vendor updateVendor(Long id, Vendor input) {
        Vendor vendor = getVendorById(id);
        vendor.setName(input.getName());
        vendor.setEmail(input.getEmail());
        vendor.setPhone(input.getPhone());
        return vendorRepository.save(vendor);
    }
}
