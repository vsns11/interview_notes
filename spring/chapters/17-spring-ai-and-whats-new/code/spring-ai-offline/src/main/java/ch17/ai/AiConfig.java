package ch17.ai;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    /** With a real starter on the classpath, Boot would create OpenAiChatModel / AnthropicChatModel here. */
    @Bean
    FakeChatModel chatModel() {
        return new FakeChatModel();
    }

    @Bean
    EmbeddingModel embeddingModel() {
        return new FakeEmbeddingModel();
    }

    /** In-memory vector store, pre-loaded with a few "help-centre" documents. */
    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        store.add(List.of(
                new Document("Refunds are issued to the original payment method within 5 business days.",
                        Map.of("source", "refund-policy.md")),
                new Document("Standard shipping takes 3 to 7 days. Express shipping arrives the next day.",
                        Map.of("source", "shipping.md")),
                new Document("You can change your password under Settings, then Security.",
                        Map.of("source", "account.md"))));
        return store;
    }

    /** A pre-configured client for the support use case: system prompt + memory + logging. */
    @Bean
    ChatClient supportClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultSystem("You are a polite support assistant for Acme Shop. Answer briefly.")
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new RedactingAdvisor(),
                        SimpleLoggerAdvisor.builder()         // logs at DEBUG; here: one short line each way
                                .requestToString(req -> req.prompt().getUserMessage().getText())
                                .responseToString(res -> res.getResult().getOutput().getText())
                                .build())
                .build();
    }
}
