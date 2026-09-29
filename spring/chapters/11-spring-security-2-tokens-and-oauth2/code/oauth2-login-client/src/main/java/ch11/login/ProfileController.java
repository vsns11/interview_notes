package ch11.login;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ProfileController {

    @GetMapping("/")
    String home() { return "home (public)"; }

    // After login the principal is an OidcUser built from the ID token (+ userinfo)
    @GetMapping("/profile")
    String profile(@AuthenticationPrincipal OidcUser user) {
        return "hello " + user.getFullName() + " <" + user.getEmail() + ">, sub=" + user.getSubject()
                + ", issuer=" + user.getIssuer();
    }
}
