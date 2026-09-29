package ch09.nosql;

import static org.assertj.core.api.Assertions.assertThat;

import ch09.nosql.mongo.RatingService;
import ch09.nosql.mongo.Review;
import ch09.nosql.mongo.ReviewRepository;
import ch09.nosql.redis.Cart;
import ch09.nosql.redis.CartRepository;
import ch09.nosql.redis.PageViewCounter;
import com.redis.testcontainers.RedisContainer;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

@SpringBootTest
@Testcontainers                       // starts/stops the @Container fields (needs a Docker daemon)
class NoSqlTests {

    @Container @ServiceConnection     // Boot points spring.mongodb.* at this container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8.0");

    @Container @ServiceConnection     // ...and spring.data.redis.* at this one
    static RedisContainer redis = new RedisContainer("redis:8.2");

    @Autowired ReviewRepository reviews;
    @Autowired RatingService ratings;
    @Autowired CartRepository carts;
    @Autowired PageViewCounter views;

    @Test
    void mongoRepositoryAndAggregation() {
        reviews.deleteAll();
        reviews.save(new Review(null, "BOOK-1", 5, "Great", List.of("clear"), Instant.now()));
        reviews.save(new Review(null, "BOOK-1", 3, "Dense", List.of(), Instant.now()));
        reviews.save(new Review(null, "PEN-7", 4, "Smooth", List.of("clear"), Instant.now()));

        assertThat(reviews.findByProductSkuOrderByCreatedAtDesc("BOOK-1")).hasSize(2);
        assertThat(reviews.withTag("clear")).hasSize(2);
        assertThat(ratings.averageByProduct()).first()
                .isEqualTo(new RatingService.AverageRating("PEN-7", 4.0, 1));
    }

    @Test
    void redisRepositoryAndCounter() {
        carts.save(new Cart("c1", "u42", List.of(new Cart.CartItem("PEN-7", 3))));
        assertThat(carts.findByUserId("u42")).extracting(Cart::id).containsExactly("c1");
        assertThat(views.hit("home")).isEqualTo(1);
        assertThat(views.hit("home")).isEqualTo(2);
    }
}
