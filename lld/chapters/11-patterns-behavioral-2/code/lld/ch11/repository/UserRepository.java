package lld.ch11.repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY: a collection-like interface for aggregates, in DOMAIN language.
 * The domain depends on this; the storage details live in implementations.
 */
public interface UserRepository {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    List<User> findActive();
    boolean deleteById(String id);
}
