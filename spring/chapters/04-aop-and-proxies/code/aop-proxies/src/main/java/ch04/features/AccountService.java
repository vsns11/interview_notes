package ch04.features;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class AccountService {

    private final ObjectProvider<AccountService> self;   // lazy handle to *our own proxy*

    public AccountService(ObjectProvider<AccountService> self) {
        this.self = self;
    }

    @Transactional
    public String transfer() {
        // true only if a transaction proxy started a transaction before we got here
        return "transaction active = " + TransactionSynchronizationManager.isActualTransactionActive();
    }

    public String transferViaThis() {
        return this.transfer();              // plain Java call on the raw object: skips the proxy
    }

    public String transferViaSelfProxy() {
        return self.getObject().transfer();  // goes through the proxy: advice runs
    }
}
