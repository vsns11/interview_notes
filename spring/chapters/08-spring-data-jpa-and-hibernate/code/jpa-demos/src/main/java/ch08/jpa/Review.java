package ch08.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int stars;

    @ManyToOne // left at the JPA default: EAGER. Watch what that costs in NPlusOneTest.
    private Book book;

    protected Review() {}

    public int getStars() { return stars; }
    public Book getBook() { return book; }
}
