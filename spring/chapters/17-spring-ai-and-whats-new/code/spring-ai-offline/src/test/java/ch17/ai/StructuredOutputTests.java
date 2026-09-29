package ch17.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StructuredOutputTests {

    @Autowired ChatClient.Builder builder;
    @Autowired FakeChatModel model;

    @BeforeEach
    void reset() { model.reset(); }

    @Test
    void entityConvertsTheReplyIntoARecord() {
        MovieRecommendation movie = builder.build().prompt()
                .user("Recommend one science-fiction movie about survival.")
                .call()
                .entity(MovieRecommendation.class);

        String sent = model.lastPrompt().getUserMessage().getText();
        System.out.println("user message sent to the model (first 3 lines):");
        sent.lines().limit(3).forEach(l -> System.out.println("  | " + l));
        System.out.println("  | ... (" + sent.lines().count() + " lines in total, including a JSON Schema)");
        System.out.println("parsed record: " + movie);

        assertThat(movie.title()).isEqualTo("The Martian");
        assertThat(movie.year()).isEqualTo(2015);
        assertThat(movie.genres()).containsExactly("sci-fi", "survival");
    }

    @Test
    void theConverterCanBeUsedOnItsOwn() {
        var converter = new BeanOutputConverter<>(MovieRecommendation.class);
        MovieRecommendation m = converter.convert("""
                {"title":"Arrival","year":2016,"genres":["sci-fi","drama"],"score":7.9}""");
        System.out.println("converted: " + m);
        System.out.println("schema mentions the fields: "
                + converter.getJsonSchema().contains("\"genres\"") + ", " + converter.getJsonSchema().contains("\"year\""));
        assertThat(m.year()).isEqualTo(2016);
    }
}
