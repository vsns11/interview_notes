package com.example.ioc.ambiguous;

import org.springframework.stereotype.Component;

/** Two beans of one type, no @Primary, no @Qualifier: startup must fail. */
public final class Ambiguous {
    private Ambiguous() {}
    public interface Storage {}
    @Component public static class DiskStorage implements Storage {}
    @Component public static class CloudStorage implements Storage {}
    @Component public static class Backup {
        public Backup(Storage storage) {}
    }
}
