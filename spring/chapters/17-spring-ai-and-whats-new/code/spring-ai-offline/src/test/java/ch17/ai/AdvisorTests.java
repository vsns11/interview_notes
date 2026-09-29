package ch17.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class AdvisorTests {

    @Autowired @Qualifier("supportClient") ChatClient support;   // memory + redaction + logging advisors
    @Autowired FakeChatModel model;
    @Autowired ChatMemory chatMemory;

    @BeforeEach
    void reset() {
        model.reset();
        chatMemory.clear("alice-42");
        chatMemory.clear("bob-7");
    }

    private String ask(String conversationId, String text) {
        return support.prompt()
                .user(text)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    @Test
    void memoryRemembersPerConversation() {
        System.out.println("alice> " + ask("alice-42", "Hi, my name is Alice."));
        System.out.println("alice> " + ask("alice-42", "What is my name?"));
        System.out.println("bob>   " + ask("bob-7", "What is my name?"));

        System.out.println("second call for alice-42 sent these messages to the model:");
        for (Message m : model.received().get(1).getInstructions()) {
            System.out.println("  " + m.getMessageType() + ": " + m.getText());
        }
        assertThat(model.received().get(1).getInstructions()).hasSize(4); // system, user, assistant, user
        assertThat(chatMemory.get("alice-42")).hasSize(4);                  // 2 user + 2 assistant messages
        assertThat(chatMemory.get("bob-7")).hasSize(2);
    }

    @Test
    void redactingAdvisorMasksEmailsBeforeTheModelSeesThem() {
        ask("alice-42", "Please send the invoice to alice@example.com");
        String seenByModel = model.lastPrompt().getUserMessage().getText();
        String stored = chatMemory.get("alice-42").getFirst().getText();
        System.out.println("model saw:      " + seenByModel);
        System.out.println("memory stored:  " + stored);
        assertThat(seenByModel).doesNotContain("@").contains("[email]");
        assertThat(stored).doesNotContain("@");
    }

    @Test
    void simpleLoggerAdvisorLogsRequestAndResponse(CapturedOutput output) {
        ask("bob-7", "Do you ship to Canada?");
        output.getOut().lines()
                .filter(l -> l.contains("SimpleLoggerAdvisor"))
                .map(l -> l.substring(l.indexOf("SimpleLoggerAdvisor")))
                .forEach(l -> System.out.println("LOG " + l));
        assertThat(output.getOut()).contains("request:", "response:");
    }
}
