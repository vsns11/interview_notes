package lld.ch19.amazon;

import java.util.ArrayList;
import java.util.List;

enum AccountStatus { ACTIVE, BLOCKED, BANNED, COMPROMISED, ARCHIVED, UNKNOWN }

/** Login data shared by members and admins. */
final class Account {
    private final String userName;
    private final String email;
    private final String phone;
    private final List<Address> addresses = new ArrayList<>();
    private volatile AccountStatus status = AccountStatus.ACTIVE;

    Account(String userName, String email, String phone) {
        this.userName = userName;
        this.email = email;
        this.phone = phone;
    }

    String userName() { return userName; }
    String email() { return email; }
    String phone() { return phone; }
    AccountStatus status() { return status; }
    void setStatus(AccountStatus s) { status = s; }
    synchronized void addAddress(Address a) { addresses.add(a); }
    synchronized List<Address> addresses() { return List.copyOf(addresses); }
}

/**
 * Everything any shopper may do - browse and fill a cart. Placing an order is
 * deliberately NOT here, because a Guest is a Customer and must not inherit it (LSP).
 */
abstract sealed class Customer permits Guest, Member {
    private final ShoppingCart cart = new ShoppingCart();

    ShoppingCart cart() { return cart; }
    void addToCart(Product p, int qty) { cart.addItem(p, qty); }
    void removeFromCart(String productId) { cart.removeItem(productId); }
}

final class Guest extends Customer {
    /** Registering turns a guest into a member and carries the cart across. */
    Member register(String memberId, Account account) {
        Member m = new Member(memberId, account);
        m.cart().mergeFrom(cart());
        cart().clear();
        return m;
    }
}

final class Member extends Customer {
    private final String id;
    private final Account account;

    Member(String id, Account account) {
        this.id = id;
        this.account = account;
    }

    String id() { return id; }
    Account account() { return account; }
    boolean canBuy() { return account.status() == AccountStatus.ACTIVE; }
}

final class Admin {
    private final Account account;

    Admin(Account account) { this.account = account; }

    ProductCategory addCategory(String name, String description) { return new ProductCategory(name, description); }

    void block(Member m) { m.account().setStatus(AccountStatus.BLOCKED); }

    void unblock(Member m) { m.account().setStatus(AccountStatus.ACTIVE); }

    String name() { return account.userName(); }
}
