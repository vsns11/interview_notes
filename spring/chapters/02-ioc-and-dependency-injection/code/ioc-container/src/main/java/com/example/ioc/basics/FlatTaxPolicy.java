package com.example.ioc.basics;

import com.example.ioc.Trace;

public class FlatTaxPolicy implements TaxPolicy {
    public FlatTaxPolicy() { Trace.add("FlatTaxPolicy created"); }
    @Override public long taxCents(long netCents) { return netCents * 20 / 100; }
}
