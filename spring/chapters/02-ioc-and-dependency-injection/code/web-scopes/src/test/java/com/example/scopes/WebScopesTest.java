package com.example.scopes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class WebScopesTest {

    @Autowired MockMvcTester mvc;

    String post(String item, MockHttpSession session) {
        var r = mvc.post().uri("/cart/" + item).session(session).exchange();
        assertThat(r).hasStatusOk();
        try { return r.getResponse().getContentAsString(); } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Test
    void requestAndSessionScopes() {
        var alice = new MockHttpSession();
        var bob = new MockHttpSession();
        String a1 = post("book", alice), a2 = post("pen", alice), b1 = post("mug", bob);
        System.out.println("alice: " + a1);
        System.out.println("alice: " + a2);
        System.out.println("bob:   " + b1);
        assertThat(a2).contains("cart=[book, pen]");   // same session -> same Cart
        assertThat(b1).contains("cart=[mug]");         // new session  -> new Cart
        assertThat(a1).isNotEqualTo(a2);               // every request -> new RequestInfo
    }
}
