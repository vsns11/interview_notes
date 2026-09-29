package ch08.jpa;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Touches a lazy collection OUTSIDE any @Transactional method - only works with open-in-view. */
@RestController
class AuthorController {

    private final AuthorRepository authors;

    AuthorController(AuthorRepository authors) {
        this.authors = authors;
    }

    @GetMapping("/authors/{id}/summary")
    String summary(@PathVariable Long id) {
        Author a = authors.findById(id).orElseThrow();
        return a.getName() + " has " + a.getBooks().size() + " books";
    }
}
