package lld.ch02.ooad;

/**
 * The CONNECTION OBJECT: "a borrowed book" carries its own data (borrower, due day),
 * which belongs neither on Book nor on Member.
 */
public record Loan(Book book, Member member, int dueDay) {
    public boolean isLate(int today) { return today > dueDay; }
}
