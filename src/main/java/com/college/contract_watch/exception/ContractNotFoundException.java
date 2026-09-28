package com.college.contract_watch.exception;

public class ContractNotFoundException extends RuntimeException {

    public ContractNotFoundException(Long id) {
        super("Contract not found with id: " + id);
    }
}
