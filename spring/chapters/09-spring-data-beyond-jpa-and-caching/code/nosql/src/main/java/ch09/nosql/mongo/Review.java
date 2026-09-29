package ch09.nosql.mongo;

import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** One MongoDB document. Nested data (tags) is stored inside it - no join table. */
@Document("reviews")
public record Review(
        @Id String id,                 // Mongo's _id; null -> an ObjectId is generated on insert
        @Indexed String productSku,    // index is created only if spring.data.mongodb.auto-index-creation=true
        int stars,
        String text,
        List<String> tags,
        Instant createdAt) {}
