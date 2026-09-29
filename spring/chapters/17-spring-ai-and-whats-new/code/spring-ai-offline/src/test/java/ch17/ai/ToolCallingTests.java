package ch17.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ToolCallingTests {

    @Autowired ChatClient.Builder builder;
    @Autowired FakeChatModel model;

    @BeforeEach
    void reset() { model.reset(); }

    @Test
    void modelRequestsAToolSpringRunsItAndSendsTheResultBack() {
        OrderTools tools = new OrderTools();
        String answer = builder.build().prompt()
                .user("Where is my order A-1001?")
                .tools(tools)                          // expose the @Tool methods for this request
                .call()
                .content();

        List<Prompt> round = model.received();
        System.out.println("model calls: " + round.size());
        var offered = ((ToolCallingChatOptions) round.getFirst().getOptions()).getToolCallbacks();
        System.out.println("1) tools offered to the model: "
                + offered.stream().map(t -> t.getToolDefinition().name()).toList());
        for (Message m : round.get(1).getInstructions()) {
            if (m instanceof AssistantMessage am && am.hasToolCalls()) {
                var call = am.getToolCalls().getFirst();
                System.out.println("2) model asked for: " + call.name() + " " + call.arguments());
            }
            if (m instanceof ToolResponseMessage tr) {
                System.out.println("3) Spring ran the method, sent back: " + tr.getResponses().getFirst().responseData());
            }
        }
        System.out.println("4) final answer: " + answer);

        assertThat(round).hasSize(2);
        assertThat(tools.calls).containsExactly("A-1001");
        assertThat(answer).contains("SHIPPED");
    }

    @Test
    void withoutToolsTheModelCannotLookAnythingUp() {
        String answer = builder.build().prompt().user("Where is my order A-1001?").call().content();
        System.out.println("no tools -> " + answer);
        assertThat(model.received()).hasSize(1);
    }
}
