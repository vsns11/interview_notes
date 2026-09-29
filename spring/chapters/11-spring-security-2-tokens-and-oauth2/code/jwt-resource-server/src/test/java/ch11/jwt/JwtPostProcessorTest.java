package ch11.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** jwt() skips decoding and signature checks: it puts a ready-made JwtAuthenticationToken in the context. */
@SpringBootTest
@AutoConfigureMockMvc
class JwtPostProcessorTest {

    @Autowired MockMvcTester mvc;
    @Autowired ScopeAndRoleAuthorities appConverter;

    @Test
    void jwtPostProcessor() throws Exception {
        var plain = mvc.get().uri("/api/messages").with(jwt()).exchange();          // sub=user, scope=read
        var explicit = mvc.post().uri("/api/messages").contentType(MediaType.TEXT_PLAIN).content("x")
                .with(jwt().jwt(j -> j.subject("carol"))
                        .authorities(new SimpleGrantedAuthority("SCOPE_messages.write"))).exchange();
        var scopeClaim = mvc.get().uri("/api/messages")
                .with(jwt().jwt(j -> j.claim("scope", "messages.read"))).exchange();
        // PITFALL: jwt() does not use your JwtAuthenticationConverter unless you pass it in
        var rolesIgnored = mvc.get().uri("/api/admin/audit")
                .with(jwt().jwt(j -> j.claim("roles", List.of("ADMIN")))).exchange();
        var rolesMapped = mvc.get().uri("/api/admin/audit")
                .with(jwt().jwt(j -> j.claim("roles", List.of("ADMIN"))).authorities(appConverter)).exchange();

        System.out.println("jwt() default, GET messages             -> " + plain.getResponse().getStatus());
        System.out.println("jwt() SCOPE_messages.write, POST        -> " + explicit.getResponse().getStatus()
                + " " + explicit.getResponse().getContentAsString());
        System.out.println("jwt() scope claim messages.read, GET    -> " + scopeClaim.getResponse().getStatus());
        System.out.println("jwt() roles=[ADMIN], default converter  -> " + rolesIgnored.getResponse().getStatus());
        System.out.println("jwt() roles=[ADMIN], app converter      -> " + rolesMapped.getResponse().getStatus());

        assertThat(explicit).hasStatus(201);
        assertThat(scopeClaim).hasStatusOk();
        assertThat(rolesIgnored).hasStatus(403);
        assertThat(rolesMapped).hasStatusOk();
    }
}
