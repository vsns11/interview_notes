package lld.ch15.library;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The three core flows (check out, return, renew) plus reservations.
 * Lives apart from Member so a Member stays "a person", and the rules live in one place.
 *
 * Locking rule (prevents deadlock): always lock the MEMBER first, then the BOOK ITEM.
 */
public final class LendingService {
    static final int MAX_BOOKS_PER_MEMBER = 5;
    static final int MAX_LENDING_DAYS = 10;

    private final Catalog catalog;
    private final Clock clock;
    private final FinePolicy finePolicy;
    private final Notifier notifier;
    private final Map<String, Member> members = new ConcurrentHashMap<>();
    private final Map<String, BookLending> activeLoans = new ConcurrentHashMap<>();    // barcode -> loan
    private final Map<String, Deque<String>> waitlists = new ConcurrentHashMap<>();    // isbn -> member ids (FIFO)
    private final List<Fine> fines = Collections.synchronizedList(new ArrayList<>());

    LendingService(Catalog catalog, Clock clock, FinePolicy finePolicy, Notifier notifier) {
        this.catalog = catalog; this.clock = clock; this.finePolicy = finePolicy; this.notifier = notifier;
    }

    void register(Member m) { members.put(m.id(), m); }

    /** Cancel a membership. Refused while the member still has books out; drops them from every waitlist. */
    void cancelMembership(String memberId) {
        Member m = member(memberId);
        synchronized (m) {
            if (m.booksCheckedOut() > 0)
                throw new LibraryException(memberId + " must return " + m.booksCheckedOut() + " book(s) first");
            m.setStatus(AccountStatus.CANCELED);
        }
        for (Deque<String> q : waitlists.values()) synchronized (q) { q.remove(memberId); }
    }

    private LocalDate today() { return LocalDate.now(clock); }

    private Member member(String id) {
        Member m = members.get(id);
        if (m == null) throw new LibraryException("unknown member " + id);
        return m;
    }

    // ---------------------------------------------------------------- check out
    BookLending checkout(String memberId, String barcode) {
        Member m = member(memberId);
        BookItem item = catalog.item(barcode);
        synchronized (m) {
            synchronized (item) {
                if (m.status() != AccountStatus.ACTIVE)
                    throw new LibraryException(memberId + " account is " + m.status());
                if (m.booksCheckedOut() >= MAX_BOOKS_PER_MEMBER)
                    throw new LibraryException(memberId + " already has " + MAX_BOOKS_PER_MEMBER + " books");
                switch (item.status()) {
                    case AVAILABLE -> { /* ok */ }
                    case RESERVED -> {
                        if (!memberId.equals(item.heldForMemberId()))
                            throw new LibraryException(barcode + " is held for another member");
                    }
                    case LOANED, LOST -> throw new LibraryException(barcode + " is " + item.status());
                }
                item.markLoaned();
                m.incrementLoans();
                LocalDate today = today();
                BookLending loan = new BookLending(barcode, memberId, today, today.plusDays(MAX_LENDING_DAYS));
                activeLoans.put(barcode, loan);
                removeFromWaitlist(item.book().isbn(), memberId);   // their reservation is now fulfilled
                return loan;
            }
        }
    }

    // ---------------------------------------------------------------- return
    BigDecimal returnBook(String barcode) {
        BookLending loan = activeLoans.get(barcode);
        if (loan == null) throw new LibraryException(barcode + " is not checked out");
        Member m = member(loan.memberId());
        BookItem item = catalog.item(barcode);
        synchronized (m) {
            synchronized (item) {
                if (activeLoans.get(barcode) != loan)                // someone returned it meanwhile
                    throw new LibraryException(barcode + " is not checked out");
                LocalDate today = today();
                loan.close(today);
                activeLoans.remove(barcode);
                m.decrementLoans();
                BigDecimal fine = chargeFineIfLate(loan, today);
                handToNextInLine(item);
                return fine;
            }
        }
    }

    // ---------------------------------------------------------------- renew
    BookLending renew(String memberId, String barcode) {
        Member m = member(memberId);
        BookItem item = catalog.item(barcode);
        synchronized (m) {
            synchronized (item) {
                BookLending loan = activeLoans.get(barcode);
                if (loan == null || !loan.memberId().equals(memberId))
                    throw new LibraryException(memberId + " does not have " + barcode);
                if (waitingCount(item.book().isbn()) > 0)
                    throw new LibraryException("cannot renew " + barcode + ": another member reserved this title");
                LocalDate today = today();
                chargeFineIfLate(loan, today);                       // a late renewal still pays the fine
                loan.renewUntil(today.plusDays(MAX_LENDING_DAYS));
                return loan;
            }
        }
    }

    // ---------------------------------------------------------------- reserve
    /** Join the FIFO waitlist for a TITLE that has no free copy. Returns the 1-based position. */
    int reserve(String memberId, String isbn) {
        Member m = member(memberId);
        if (m.status() != AccountStatus.ACTIVE) throw new LibraryException(memberId + " account is " + m.status());
        if (catalog.copiesOf(isbn).isEmpty()) throw new LibraryException("unknown isbn " + isbn);
        if (catalog.availableCopies(isbn) > 0)
            throw new LibraryException("a copy is on the shelf, just check it out");
        Deque<String> q = waitlist(isbn);
        synchronized (q) {
            if (!q.contains(memberId)) q.addLast(memberId);
            return new ArrayList<>(q).indexOf(memberId) + 1;
        }
    }

    // ---------------------------------------------------------------- system jobs & queries
    void sendOverdueNotices() {
        LocalDate today = today();
        activeLoans.values().stream()
                .filter(l -> l.dueDate().isBefore(today))
                .sorted((a, b) -> a.barcode().compareTo(b.barcode()))
                .forEach(l -> notifier.send(l.memberId(), "'" + catalog.item(l.barcode()).book().title()
                        + "' (" + l.barcode() + ") was due " + l.dueDate()));
    }

    String whoHas(String barcode) {
        BookLending l = activeLoans.get(barcode);
        return l == null ? null : l.memberId();
    }

    List<String> loansOf(String memberId) {
        return activeLoans.values().stream().filter(l -> l.memberId().equals(memberId))
                .map(BookLending::barcode).sorted().toList();
    }

    List<Fine> fines() { synchronized (fines) { return List.copyOf(fines); } }

    // ---------------------------------------------------------------- helpers
    private BigDecimal chargeFineIfLate(BookLending loan, LocalDate today) {
        BigDecimal amount = finePolicy.fine(loan.dueDate(), today);
        if (amount.signum() > 0) {
            fines.add(new Fine(loan.memberId(), loan.barcode(), amount, today));
            notifier.send(loan.memberId(), "fine of " + Money.fmt(amount) + " for late " + loan.barcode());
        }
        return amount;
    }

    /** Caller holds the item's monitor. Give the copy to the first waiting member, or shelve it. */
    private void handToNextInLine(BookItem item) {
        Deque<String> q = waitlist(item.book().isbn());
        String next;
        synchronized (q) { next = q.pollFirst(); }
        if (next == null) { item.markAvailable(); return; }
        item.holdFor(next);
        notifier.send(next, "'" + item.book().title() + "' copy " + item.barcode() + " is held for you");
    }

    private Deque<String> waitlist(String isbn) {
        return waitlists.computeIfAbsent(isbn, k -> new ArrayDeque<>());
    }

    int waitingCount(String isbn) {
        Deque<String> q = waitlist(isbn);
        synchronized (q) { return q.size(); }
    }

    private void removeFromWaitlist(String isbn, String memberId) {
        Deque<String> q = waitlist(isbn);
        synchronized (q) { q.remove(memberId); }
    }
}
