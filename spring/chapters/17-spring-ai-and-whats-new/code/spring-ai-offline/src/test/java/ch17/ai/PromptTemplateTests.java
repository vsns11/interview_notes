package ch17.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ClassPathResource;

class PromptTemplateTests {

    @Test
    void rendersPlaceholders() {
        PromptTemplate template = new PromptTemplate(
                "Explain {topic} to a {audience} in at most {words} words.");
        String text = template.render(Map.of("topic", "dependency injection", "audience", "beginner", "words", 50));
        System.out.println("rendered: " + text);
        assertThat(text).isEqualTo("Explain dependency injection to a beginner in at most 50 words.");
    }

    @Test
    void templateFromClasspathFileBecomesAPrompt() {
        PromptTemplate template = new PromptTemplate(new ClassPathResource("prompts/explain.st"));
        Prompt prompt = template.create(Map.of("topic", "virtual threads", "audience", "Java developer", "words", 80));
        System.out.println("prompt messages: " + prompt.getInstructions().size()
                + ", user text: " + prompt.getUserMessage().getText().strip());
        assertThat(prompt.getUserMessage().getText()).contains("virtual threads", "80 words");
    }

    @Test
    void missingVariableFailsFast() {
        PromptTemplate template = new PromptTemplate("Hello {name}, your plan is {plan}.");
        try {
            template.render(Map.of("name", "Ana"));
        } catch (IllegalStateException e) {
            System.out.println("missing variable -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
            assertThat(e.getMessage()).contains("plan");
            return;
        }
        throw new AssertionError("expected an exception");
    }
}
