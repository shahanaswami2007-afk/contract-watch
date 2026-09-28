package com.college.contract_watch.exception;

public class VendorNotFoundException extends RuntimeException {

    public VendorNotFoundException(Long id) {
        super("Vendor not found with id: " + id);
    }
}
