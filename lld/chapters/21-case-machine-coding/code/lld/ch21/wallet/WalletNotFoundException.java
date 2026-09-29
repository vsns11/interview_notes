package lld.ch21.wallet;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String id) { super("Wallet not found: " + id); }
}
