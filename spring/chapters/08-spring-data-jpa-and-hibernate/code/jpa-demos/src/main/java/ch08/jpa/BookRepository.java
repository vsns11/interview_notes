package ch08.jpa;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    // ---- derived queries: the method name IS the query ----
    List<Book> findByTitleContainingIgnoreCase(String part);

    List<Book> findByPriceLessThanOrderByPriceAsc(BigDecimal max);

    List<Book> findByAuthorName(String authorName);          // traverses book.author.name (a join)

    long countByAuthorName(String authorName);

    boolean existsByTitle(String title);

    List<Book> findTop2ByOrderByPriceDesc();

    List<Book> findByTagsName(String tag, Sort sort);        // many-to-many join + dynamic sort

    // ---- pagination ----
    Page<Book> findByPriceGreaterThan(BigDecimal min, Pageable pageable);   // also runs a COUNT query

    Slice<Book> findByAuthorIdIn(List<Long> authorIds, Pageable pageable);  // no COUNT, fetches size+1 rows

    // ---- @Query: JPQL and native SQL ----
    @Query("select b from Book b where b.author.name = :name and b.price <= :max order by b.price")
    List<Book> byAuthorUpTo(@Param("name") String name, @Param("max") BigDecimal max);

    @Query(value = "SELECT title FROM book WHERE price > ?1 ORDER BY title", nativeQuery = true)
    List<String> titlesAbove(BigDecimal min);

    @Modifying(clearAutomatically = true) // bulk UPDATE bypasses the persistence context -> clear it
    @Query("update Book b set b.price = b.price * :factor where b.author.id = :authorId")
    int reprice(@Param("authorId") Long authorId, @Param("factor") BigDecimal factor);

    // ---- projections ----
    List<BookSummary> findByAuthorNameOrderByTitle(String authorName);      // interface projection

    List<BookRow> findByPriceLessThan(BigDecimal max);                       // record (DTO) projection

    @Query("select new ch08.jpa.BookRow(b.title, b.price) from Book b where b.author.id = :id order by b.title")
    List<BookRow> rowsForAuthor(@Param("id") Long authorId);                 // constructor expression

    <T> List<T> findByAuthorId(Long authorId, Class<T> type);               // dynamic projection

    // ---- pessimistic locking: SELECT ... FOR UPDATE ----
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Book> findWithLockById(Long id);
}
