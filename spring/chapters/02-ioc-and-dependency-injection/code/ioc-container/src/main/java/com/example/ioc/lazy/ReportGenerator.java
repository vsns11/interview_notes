package com.example.ioc.lazy;

import com.example.ioc.Trace;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy                                        // not created at startup; created on first use
public class ReportGenerator {
    public ReportGenerator() { Trace.add("ReportGenerator created (lazy)"); }
}
