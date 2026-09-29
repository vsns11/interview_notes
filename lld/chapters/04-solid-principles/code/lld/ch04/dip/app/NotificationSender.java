package lld.ch04.dip.app;

/**
 * The abstraction. Note the PACKAGE: it lives with the high-level policy (app),
 * not with the implementations (infra). The policy owns the contract.
 */
public interface NotificationSender {
    void send(String recipient, String message);
}
