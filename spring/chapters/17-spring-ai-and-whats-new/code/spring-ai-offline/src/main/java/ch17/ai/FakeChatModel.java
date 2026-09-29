package ch17.ai;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;

/**
 * A stand-in for a real LLM. It implements the same ChatModel interface that
 * OpenAiChatModel / AnthropicChatModel implement, but answers with canned,
 * rule-based replies - so everything above it (ChatClient, advisors, tools,
 * structured output, RAG) runs for real without an API key or network.
 * It also records every Prompt it receives, so tests can print exactly
 * what the advisors sent to "the model".
 */
public class FakeChatModel implements ChatModel {

    private static final Pattern ORDER_ID = Pattern.compile("\\b([A-Z]-\\d{4})\\b");
    private static final Pattern NAME = Pattern.compile("(?i)my name is (\\w+)");

    private final List<Prompt> received = new CopyOnWriteArrayList<>();

    @Override
    public ChatResponse call(Prompt prompt) {
        received.add(prompt);
        return new ChatResponse(List.of(new Generation(reply(prompt))));
    }

    /** Tool-calling-capable default options, like a real provider's options class. */
    @Override
    public ChatOptions getOptions() {
        return ToolCallingChatOptions.builder().build();
    }

    public List<Prompt> received() { return received; }

    public Prompt lastPrompt() { return received.getLast(); }

    public void reset() { received.clear(); }

    // ----- the "brain": a few deterministic rules ------------------------------------

    private AssistantMessage reply(Prompt prompt) {
        List<Message> messages = prompt.getInstructions();
        Message last = messages.getLast();

        // Rule 1: a tool has just run - turn its JSON result into a sentence.
        if (last instanceof ToolResponseMessage toolResult) {
            var r = toolResult.getResponses().getFirst();
            return text("Here is what I found via " + r.name() + ": " + r.responseData());
        }

        String user = prompt.getUserMessage().getText();

        // Rule 2: tools are on offer and the user asks about an order -> request a tool call.
        Matcher order = ORDER_ID.matcher(user);
        String tool = offeredTool(prompt, "orderStatus");
        if (tool != null && order.find()) {
            var call = new AssistantMessage.ToolCall("call_1", "function", tool,
                    "{\"orderId\":\"" + order.group(1) + "\"}");
            return AssistantMessage.builder().content("").toolCalls(List.of(call)).build();
        }

        // Rule 3: the prompt asks for JSON matching a schema -> reply with JSON (in a code fence, as real models often do).
        if (user.contains("JSON Schema")) {
            return text("""
                    ```json
                    {"title":"The Martian","year":2015,"genres":["sci-fi","survival"],"score":8.0}
                    ```""");
        }

        // Rule 4: RAG - the advisor pasted retrieved context into the prompt -> answer from its first line.
        //         (QuestionAnswerAdvisor's template wraps the context in lines of 21 dashes.)
        String[] parts = user.split("-{21}");
        if (parts.length >= 4) {
            String firstFact = parts[2].lines().filter(l -> !l.isBlank()).findFirst().orElse("nothing");
            return text("According to our docs: " + firstFact);
        }

        // Rule 5: memory - answer "what is my name?" from earlier messages in the prompt.
        if (user.toLowerCase().contains("what is my name")) {
            String name = messages.stream()
                    .filter(m -> m instanceof UserMessage)
                    .map(m -> NAME.matcher(m.getText()))
                    .filter(Matcher::find)
                    .map(m -> m.group(1))
                    .reduce((a, b) -> b)
                    .orElse(null);
            return text(name == null ? "I don't know your name." : "Your name is " + name + ".");
        }

        // Default: a canned answer that shows what the model "heard".
        return text("(fake model) You asked: " + user);
    }

    private static String offeredTool(Prompt prompt, String wanted) {
        if (prompt.getOptions() instanceof ToolCallingChatOptions opts && opts.getToolCallbacks() != null) {
            for (ToolCallback cb : opts.getToolCallbacks()) {
                if (cb.getToolDefinition().name().equals(wanted)) return wanted;
            }
        }
        return null;
    }

    private static AssistantMessage text(String s) { return new AssistantMessage(s); }
}
