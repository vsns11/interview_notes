package ch11.variants;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class Controllers {

    @GetMapping("/opaque/notes")
    String notes(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal p) {
        return "notes for " + p.getName() + " (scope=" + p.getAttribute("scope") + ")";
    }

    @PostMapping("/opaque/notes/write")
    String write(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal p) { return "written by " + p.getName(); }

    @GetMapping("/tenant/whoami")
    String whoami(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getSubject() + " from " + jwt.getIssuer();
    }
}
