package ch10.basics;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Our own user store. In a real app the map would be a table read through a repository. */
@Service
public class AccountUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

    /** What we keep per user: a password HASH (never the password) and granted authorities. */
    public record Account(String username, String passwordHash, List<String> authorities, boolean enabled) {}

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    AccountUserDetailsService(PasswordEncoder encoder) {
        save(new Account("alice", encoder.encode("alice-pass"), List.of("ROLE_USER", "report:read"), true));
        save(new Account("bob", encoder.encode("bob-pass"), List.of("ROLE_ADMIN"), true));
        save(new Account("carol", encoder.encode("carol-pass"), List.of("ROLE_USER"), true));
        save(new Account("dave", "{noop}dave-pass", List.of("ROLE_USER"), true));   // legacy plain-text row
        save(new Account("erin", encoder.encode("erin-pass"), List.of("ROLE_USER"), false)); // disabled
    }

    private void save(Account a) { accounts.put(a.username(), a); }

    public Account find(String username) { return accounts.get(username); }

    // Called by DaoAuthenticationProvider during login
    @Override
    public UserDetails loadUserByUsername(String username) {
        Account a = accounts.get(username);
        if (a == null) throw new UsernameNotFoundException(username);
        return User.withUsername(a.username())
                .password(a.passwordHash())
                .authorities(a.authorities().toArray(String[]::new))
                .disabled(!a.enabled())
                .build();
    }

    // Called after a successful login when the stored hash is outdated ({noop}, weak bcrypt cost...)
    @Override
    public UserDetails updatePassword(UserDetails user, String newEncodedPassword) {
        Account old = accounts.get(user.getUsername());
        save(new Account(old.username(), newEncodedPassword, old.authorities(), old.enabled()));
        return User.withUserDetails(user).password(newEncodedPassword).build();
    }
}
