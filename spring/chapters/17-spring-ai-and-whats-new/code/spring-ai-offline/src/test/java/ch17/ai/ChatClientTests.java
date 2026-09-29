package ch17.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

@SpringBootTest
class ChatClientTests {

    @Autowired ChatClient.Builder builder;    // auto-configured by Spring AI from the ChatModel bean
    @Autowired FakeChatModel model;
    @Value("classpath:prompts/explain.st") Resource explainTemplate;

    @BeforeEach
    void reset() { model.reset(); }

    @Test
    void simplestCall() {
        ChatClient chat = builder.build();
        String answer = chat.prompt()
                .user("What is Spring AI?")
                .call()
                .content();
        System.out.println("answer: " + answer);
        assertThat(answer).isEqualTo("(fake model) You asked: What is Spring AI?");
    }

    @Test
    void systemPromptAndTemplateParameters() {
        ChatClient chat = builder.defaultSystem("You are a concise Java tutor.").build();
        chat.prompt()
                .user(u -> u.text(explainTemplate)
                        .param("topic", "records")
                        .param("audience", "beginner")
                        .param("words", 40))
                .call()
                .content();
        System.out.println("messages the model received:");
        for (Message m : model.lastPrompt().getInstructions()) {
            System.out.println("  " + m.getMessageType() + ": " + m.getText().strip());
        }
        assertThat(model.lastPrompt().getSystemMessage().getText()).isEqualTo("You are a concise Java tutor.");
        assertThat(model.lastPrompt().getUserMessage().getText()).contains("Explain records to a beginner");
    }
}
