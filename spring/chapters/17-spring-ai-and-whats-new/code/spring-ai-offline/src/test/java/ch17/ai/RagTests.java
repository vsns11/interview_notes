package ch17.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RagTests {

    @Autowired VectorStore vectorStore;
    @Autowired ChatClient.Builder builder;
    @Autowired FakeChatModel model;

    @BeforeEach
    void reset() { model.reset(); }

    @Test
    void similaritySearchFindsTheRelevantDocument() {
        List<Document> hits = vectorStore.similaritySearch(
                SearchRequest.builder().query("How long do refunds take?").topK(2).build());
        hits.forEach(d -> System.out.printf("score %.3f  %-18s %s%n",
                d.getScore(), d.getMetadata().get("source"), d.getText()));
        assertThat(hits.getFirst().getMetadata().get("source")).isEqualTo("refund-policy.md");

        List<Document> filtered = vectorStore.similaritySearch(SearchRequest.builder()
                .query("How long do refunds take?").topK(2).similarityThreshold(0.1).build());
        System.out.println("with similarityThreshold(0.1): " + filtered.size() + " document(s)");
        assertThat(filtered).hasSize(1);
    }

    @Test
    void questionAnswerAdvisorAddsRetrievedContextToThePrompt() {
        ChatClient chat = builder
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder().topK(1).similarityThreshold(0.2).build())
                        .build())
                .build();

        String answer = chat.prompt().user("How fast is express shipping?").call().content();

        String sent = model.lastPrompt().getUserMessage().getText();
        System.out.println("augmented user message:");
        sent.lines().limit(8).forEach(l -> System.out.println("  | " + l));
        System.out.println("  | ...");
        System.out.println("answer: " + answer);
        assertThat(answer).contains("Express shipping arrives the next day");
    }
}
