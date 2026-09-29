package ch09.nosql.redis;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface CartRepository extends CrudRepository<Cart, String> {

    List<Cart> findByUserId(String userId);   // works only because userId is @Indexed (a secondary index set)
}
