package lld.ch21.wallet;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String walletId, BigDecimal balance, BigDecimal needed) {
        super("Insufficient funds in " + walletId + ": balance " + balance + ", needed " + needed);
    }
}
