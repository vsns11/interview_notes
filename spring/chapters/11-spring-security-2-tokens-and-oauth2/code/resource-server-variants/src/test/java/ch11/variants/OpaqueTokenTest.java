package ch11.variants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest
@AutoConfigureMockMvc
class OpaqueTokenTest {

    @Autowired MockMvcTester mvc;

    MockHttpServletResponse call(String method, String url, String token) {
        var req = method.equals("GET") ? mvc.get().uri(url) : mvc.post().uri(url);
        return req.header("Authorization", "Bearer " + token).exchange().getResponse();
    }

    @Test
    void introspection() throws Exception {
        var alice = call("GET", "/opaque/notes", "tok-alice-7f3a");
        var aliceWrite = call("POST", "/opaque/notes/write", "tok-alice-7f3a");
        var bobWrite = call("POST", "/opaque/notes/write", "tok-bob-91c2");
        var revoked = call("GET", "/opaque/notes", "tok-revoked-0000");
        System.out.println("alice GET   -> " + alice.getStatus() + " " + alice.getContentAsString());
        System.out.println("alice write -> " + aliceWrite.getStatus());
        System.out.println("bob write   -> " + bobWrite.getStatus() + " " + bobWrite.getContentAsString());
        System.out.println("inactive    -> " + revoked.getStatus() + " " + revoked.getHeader("WWW-Authenticate"));
        assertThat(alice.getStatus()).isEqualTo(200);
        assertThat(aliceWrite.getStatus()).isEqualTo(403);
        assertThat(bobWrite.getStatus()).isEqualTo(200);
        assertThat(revoked.getStatus()).isEqualTo(401);
    }

    @Test
    void opaqueTokenPostProcessor() throws Exception {
        // Skips the introspector entirely, like jwt() skips the decoder
        var r = mvc.get().uri("/opaque/notes")
                .with(opaqueToken().attributes(a -> a.putAll(Map.of("sub", "carol", "scope", "notes.read")))
                        .authorities(new SimpleGrantedAuthority("SCOPE_notes.read"))).exchange();
        System.out.println("opaqueToken() -> " + r.getResponse().getStatus() + " " + r.getResponse().getContentAsString());
        assertThat(r).hasStatusOk();
    }
}
