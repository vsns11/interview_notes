package ch11.jwt;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class MessageController {

    private final List<String> messages = new CopyOnWriteArrayList<>(List.of("welcome"));

    @GetMapping("/api/messages")
    List<String> list() { return messages; }

    @PostMapping("/api/messages")
    @ResponseStatus(HttpStatus.CREATED)
    String add(@RequestBody String text, @AuthenticationPrincipal Jwt jwt) {
        messages.add(text);
        return "added by " + jwt.getSubject();
    }

    @GetMapping("/api/admin/audit")
    String audit(@AuthenticationPrincipal Jwt jwt) { return "audit log for " + jwt.getSubject(); }
}
