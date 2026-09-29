package lld.ch08.singleton;

import java.util.Map;

/** The dependency-injection alternative: "one instance" is decided by the wiring code, not the class. */
interface Config {
    String get(String key);
}

record MapConfig(Map<String, String> values) implements Config {
    public String get(String key) { return values.get(key); }
}

final class BillingService {
    private final Config config;                       // dependency is visible in the constructor
    BillingService(Config config) { this.config = config; }
    String charge(int cents) { return "charged " + cents + " cents in " + config.get("region"); }
}

final class ReportJob {
    private final Config config;
    ReportJob(Config config) { this.config = config; }
    String run() { return "report for " + config.get("region"); }
}
