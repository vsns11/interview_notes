package com.example.ioc.lazy;

import com.example.ioc.Trace;
import org.springframework.stereotype.Component;

@Component                                   // eager by default
public class RateCache {
    public RateCache() { Trace.add("RateCache created (eager)"); }
}
