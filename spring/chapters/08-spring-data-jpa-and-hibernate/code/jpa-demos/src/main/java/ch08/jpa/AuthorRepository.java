package ch08.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    // Fix 1 for N+1: JOIN FETCH loads authors and their books in ONE query.
    @Query("select a from Author a join fetch a.books order by a.id")
    List<Author> findAllWithBooks();

    // Fix 2: an entity graph on a repository method - same effect, no JPQL join needed.
    @EntityGraph(attributePaths = "books")
    @Query("select a from Author a order by a.id")
    List<Author> findAllWithBooksGraph();

    // Paging a query that JOIN FETCHes a collection: where does LIMIT go?
    @Query(value = "select a from Author a join fetch a.books",
           countQuery = "select count(a) from Author a")
    Page<Author> pageWithBooks(Pageable pageable);

    @EntityGraph(attributePaths = "books")
    Optional<Author> findWithBooksById(Long id);
}
