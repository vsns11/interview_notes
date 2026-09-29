package com.example.ioc.definitions.scan;

import org.springframework.stereotype.Service;

@Service               // business-logic bean
public class InvoiceService {
    private final InvoiceRepository repository;
    public InvoiceService(InvoiceRepository repository) { this.repository = repository; }
    public InvoiceRepository repository() { return repository; }
}
