package ch22.unisexbathroom;

/**
 * Unisex bathroom: at most CAPACITY people inside, and never men and women at the same time.
 * One monitor (synchronized + wait/notifyAll) guards all the state.
 *
 * fair == false: anyone of the gender already inside may enter while there is room
 *                (simple, but the other gender can starve).
 * fair == true:  "batches". Once someone of the other gender is waiting, the current gender may
 *                still admit people only until CAPACITY have entered in this turn; then the room
 *                drains and the waiting gender goes next. Nobody waits longer than one batch.
 */
public final class Bathroom {
    public enum Gender { MAN, WOMAN;
        Gender other() { return this == MAN ? WOMAN : MAN; } }

    private final int capacity;
    private final boolean fair;

    // ---- state, all guarded by 'this' ----
    private int inside;                       // people currently inside
    private Gender insideGender;              // meaningful only when inside > 0
    private final int[] waiting = new int[2]; // waiting[g.ordinal()] = people of g blocked in enter()
    private Gender nextTurn = Gender.WOMAN;   // fair mode: who goes first when the room is empty
                                              // and both genders are waiting
    private int admittedThisTurn;             // fair mode: entries since the room was last empty

    // ---- statistics for the demo (also guarded by 'this') ----
    private int maxInside;
    private int mixedViolations;

    public Bathroom(int capacity, boolean fair) {
        this.capacity = capacity;
        this.fair = fair;
    }

    private boolean mayEnter(Gender g) {
        if (inside == capacity) return false;
        boolean otherWaiting = waiting[g.other().ordinal()] > 0;
        if (inside == 0) {
            return !fair || !otherWaiting || nextTurn == g;
        }
        if (insideGender != g) return false;
        // fair: once the other side waits, our side may only finish its current batch
        return !fair || !otherWaiting || admittedThisTurn < capacity;
    }

    public synchronized void enter(Gender g) throws InterruptedException {
        waiting[g.ordinal()]++;
        try {
            while (!mayEnter(g)) {
                wait();
            }
        } finally {
            waiting[g.ordinal()]--;           // also undone if we are interrupted while waiting
            notifyAll();                      // our waiting count changed: others may re-decide
        }
        if (inside > 0 && insideGender != g) mixedViolations++;   // must never happen
        if (inside == 0) admittedThisTurn = 0;  // a new turn starts
        admittedThisTurn++;
        insideGender = g;
        inside++;
        maxInside = Math.max(maxInside, inside);
    }

    public synchronized void leave(Gender g) {
        inside--;
        if (inside == 0) nextTurn = g.other(); // the side that just finished goes to the back
        notifyAll();                          // several different conditions may now be true
    }

    public synchronized int maxInside() { return maxInside; }
    public synchronized int mixedViolations() { return mixedViolations; }
}
