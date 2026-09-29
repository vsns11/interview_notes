package lld.ch14.atm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** One machine: owns its parts, runs one session at a time, forwards buttons to its state. */
final class Atm {
    private static final AtomicLong TXN_SEQ = new AtomicLong(1);
    private final String id;
    private final BankService bank;
    private final CashDispenser dispenser;
    private final boolean verbose;
    private final List<String> journal = new ArrayList<>();   // operator / bank-manager audit log
    private AtmState state = IdleState.INSTANCE;
    private Card card;                                        // the card reader's slot
    private List<String> sessionAccounts = List.of();

    Atm(String id, BankService bank, CashDispenser dispenser, boolean verbose) {
        this.id = id;
        this.bank = bank;
        this.dispenser = dispenser;
        this.verbose = verbose;
    }

    // ----- customer buttons (one customer at a time: synchronized) -----
    synchronized void insertCard(Card c) { state.insertCard(this, c); }
    synchronized void enterPin(String pin) { state.enterPin(this, pin); }
    synchronized void perform(Transaction tx) { state.perform(this, tx); }
    synchronized void ejectCard() { state.ejectCard(this); }

    // ----- operator -----
    /** Key switch. The ATM must not shut down while serving a customer, so this only works when idle. */
    synchronized boolean setOutOfService() {
        if (state != IdleState.INSTANCE && state != OutOfServiceState.INSTANCE) {
            journal("operator: switch-off refused, session in progress");
            return false;
        }
        state = OutOfServiceState.INSTANCE;
        journal("operator: out of service");
        return true;
    }
    synchronized void refill(long noteCents, int n) {
        dispenser.refill(noteCents, n);
        journal("operator: refilled " + n + " x " + Money.fmt(noteCents));
        if (state == OutOfServiceState.INSTANCE) state = IdleState.INSTANCE;
    }

    // ----- helpers for states -----
    void setState(AtmState s) { state = s; }
    BankService bank() { return bank; }
    Card card() { return card; }
    List<String> sessionAccounts() { return sessionAccounts; }
    void acceptCard(Card c) { card = c; journal("card in " + mask(c)); }
    void startSession(List<String> accounts) { sessionAccounts = accounts; }

    void returnCard() {
        screen("Card ejected. Goodbye.");
        journal("card out " + mask(card));
        endSession();
    }

    void retainCard() { journal("card RETAINED " + mask(card)); endSession(); }

    void run(Transaction tx) {
        String txnId = id + "-" + TXN_SEQ.getAndIncrement();
        String result = tx.execute(new TxContext(bank, dispenser, txnId));
        screen(result);
        journal(txnId + " " + tx.getClass().getSimpleName() + ": " + result);
    }

    /** End of a session: back to Idle, or OutOfService if the cash ran out (never mid-session, so the card is returned first). */
    void finishSession() {
        if (dispenser.total() == 0) {
            state = OutOfServiceState.INSTANCE;
            journal("cash empty: out of service");
        } else {
            state = IdleState.INSTANCE;
        }
    }

    void screen(String msg) { if (verbose) System.out.println("   [" + id + " screen] " + msg); }
    List<String> journal() { return List.copyOf(journal); }
    String stateName() { return state.name(); }
    long cashLeft() { return dispenser.total(); }
    void dispenserJamForDemo() { dispenser.jamNextDispense(); }

    private void journal(String line) { journal.add(line); }
    private void endSession() { card = null; sessionAccounts = List.of(); }
    private static String mask(Card c) { return "****" + c.number().substring(c.number().length() - 4); }
}
