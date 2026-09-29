package ch09.nosql.mongo;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ReviewRepository extends MongoRepository<Review, String> {

    // derived queries become Mongo filters: { productSku: ?0 } sorted by createdAt desc
    List<Review> findByProductSkuOrderByCreatedAtDesc(String productSku);

    List<Review> findByStarsGreaterThanEqual(int stars);   // { stars: { $gte: ?0 } }

    @Query("{ 'tags': ?0 }")                                  // a JSON filter written by hand
    List<Review> withTag(String tag);
}
