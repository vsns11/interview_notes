package ch10.web;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class NoteController {

    private final List<String> notes = new CopyOnWriteArrayList<>();

    @GetMapping("/api/notes") List<String> list() { return notes; }

    @PostMapping("/api/notes")
    @ResponseStatus(HttpStatus.CREATED)
    String create(@RequestBody String text) { notes.add(text); return "created"; }

    // A classic HTML form post from the browser app (cookie-authenticated)
    @PostMapping("/web/notes")
    String createFromForm(@RequestParam String text) { notes.add(text); return "saved"; }

    @GetMapping("/web/home") String home() { return "home"; }
}
