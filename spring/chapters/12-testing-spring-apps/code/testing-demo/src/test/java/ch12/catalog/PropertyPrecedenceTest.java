package ch12.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

/** Who wins when the same key is set in several test places? (a tiny slice keeps it cheap) */
@JsonTest(properties = {"demo.a=from-properties-attribute", "demo.b=from-properties-attribute", "demo.d=from-properties-attribute"})
@TestPropertySource(properties = {"demo.a=from-@TestPropertySource", "demo.c=from-@TestPropertySource", "demo.d=from-@TestPropertySource"})
@ActiveProfiles("test")
class PropertyPrecedenceTest {

    @DynamicPropertySource
    static void dynamic(DynamicPropertyRegistry r) {
        r.add("demo.a", () -> "from-@DynamicPropertySource");
        r.add("demo.b", () -> "from-@DynamicPropertySource");
        r.add("demo.c", () -> "from-@DynamicPropertySource");
    }

    @Value("${demo.a}") String a;
    @Value("${demo.b}") String b;
    @Value("${demo.c}") String c;
    @Value("${demo.d}") String d;
    @Value("${catalog.home-currency}") String fromProfileFile;

    @Test
    void whoWins() {
        System.out.println("all three set demo.a   -> " + a);
        System.out.println("attribute vs dynamic   -> " + b);
        System.out.println("@TestPropertySource vs dynamic -> " + c);
        System.out.println("attribute vs @TestPropertySource -> " + d);
        System.out.println("application-test.properties    -> catalog.home-currency=" + fromProfileFile);
        assertThat(a).isEqualTo("from-@DynamicPropertySource");   // dynamic properties win
        assertThat(fromProfileFile).isEqualTo("EUR");
    }
}
