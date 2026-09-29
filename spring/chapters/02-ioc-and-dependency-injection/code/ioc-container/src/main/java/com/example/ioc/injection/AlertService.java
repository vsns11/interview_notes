package com.example.ioc.injection;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AlertService {
    final Notifier defaultNotifier;          // resolved by type -> the @Primary one
    final Notifier emailNotifier;            // resolved by name via @Qualifier
    final List<Notifier> all;                // every Notifier bean, sorted by @Order
    final Map<String, Notifier> byName;      // bean name -> bean
    final Optional<AuditTrail> audit;        // absent bean -> Optional.empty()
    final ObjectProvider<AuditTrail> auditProvider; // lazy, optional lookup

    // One constructor, so @Autowired is not needed (Spring 4.3+).
    public AlertService(Notifier defaultNotifier,
                        @Qualifier("email") Notifier emailNotifier,
                        List<Notifier> all,
                        Map<String, Notifier> byName,
                        Optional<AuditTrail> audit,
                        ObjectProvider<AuditTrail> auditProvider) {
        this.defaultNotifier = defaultNotifier;
        this.emailNotifier = emailNotifier;
        this.all = all;
        this.byName = byName;
        this.audit = audit;
        this.auditProvider = auditProvider;
    }

    public List<String> broadcast(String msg) { return all.stream().map(n -> n.send(msg)).toList(); }
    public String viaDefault(String msg) { return defaultNotifier.send(msg); }
    public String viaEmail(String msg) { return emailNotifier.send(msg); }
    public Map<String, Notifier> byName() { return byName; }
    public boolean hasAudit() { return audit.isPresent() || auditProvider.getIfAvailable() != null; }
}
