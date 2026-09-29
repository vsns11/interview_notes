package ch09.nosql.mongo;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/** MongoTemplate: for what repositories cannot express (aggregations, partial updates). */
@Service
public class RatingService {

    public record AverageRating(String id, double average, long count) {}   // id = the product sku

    private final MongoTemplate mongo;

    public RatingService(MongoTemplate mongo) { this.mongo = mongo; }

    public List<AverageRating> averageByProduct() {
        var pipeline = newAggregation(
                group("productSku").avg("stars").as("average").count().as("count"),
                sort(Sort.Direction.DESC, "average"));
        return mongo.aggregate(pipeline, "reviews", AverageRating.class).getMappedResults();
    }

    /** Update one field in place ($push) without reading and rewriting the whole document. */
    public long addTag(String reviewId, String tag) {
        return mongo.updateFirst(
                Query.query(Criteria.where("id").is(reviewId)),
                new Update().push("tags", tag),
                Review.class).getModifiedCount();
    }
}
