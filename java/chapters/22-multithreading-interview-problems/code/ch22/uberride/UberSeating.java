package ch22.uberride;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Uber ride: riders of two parties (BLUE and RED) arrive one thread each. A car takes exactly 4.
 * Allowed cars: 4 BLUE, 4 RED, or 2 + 2. Forbidden: 3 + 1 (the lone rider would be outnumbered).
 * Every rider calls seated(); exactly one rider per car calls drive(), after all 4 are seated.
 *
 * Primitives:
 *  - ReentrantLock 'boarding': protects the waiting counts AND is held by the car's "captain"
 *    until drive() is done, so the next car cannot start forming in the middle of this one.
 *  - one Semaphore(0) per party: a waiting rider blocks on it; the captain releases exactly
 *    the riders it picked. Fair semaphores = first come, first served inside a party.
 *  - CyclicBarrier(4): "all 4 have called seated()"; it resets itself for the next car.
 */
public final class UberSeating {
    public enum Party { BLUE, RED }

    public interface Car {
        void seated(Party party, String rider);   // called by every rider of the car
        void drive(String captain);               // called by exactly one rider per car
    }

    private final Car car;
    private final ReentrantLock boarding = new ReentrantLock();
    private int blueWaiting, redWaiting;          // guarded by 'boarding'
    private final Semaphore blueQueue = new Semaphore(0, true);
    private final Semaphore redQueue = new Semaphore(0, true);
    private final CyclicBarrier allSeated;

    public UberSeating(Car car, Runnable onAllSeated) {
        this.car = car;
        this.allSeated = new CyclicBarrier(4, onAllSeated);   // action runs once per car
    }

    public void ride(Party party, String rider) throws InterruptedException, BrokenBarrierException {
        boolean captain = false;
        boarding.lock();
        try {
            if (party == Party.BLUE) blueWaiting++; else redWaiting++;
            int mine = party == Party.BLUE ? blueWaiting : redWaiting;
            int theirs = party == Party.BLUE ? redWaiting : blueWaiting;
            Semaphore myQueue = party == Party.BLUE ? blueQueue : redQueue;
            Semaphore theirQueue = party == Party.BLUE ? redQueue : blueQueue;

            if (mine == 4) {                       // 4 of my party: me + 3 waiting
                myQueue.release(3);
                take(party, 4, 0);
                captain = true;
            } else if (mine == 2 && theirs >= 2) { // 2 + 2: me + 1 of mine + 2 of theirs
                myQueue.release(1);
                theirQueue.release(2);
                take(party, 2, 2);
                captain = true;
            }
            // otherwise: no legal car yet, wait for a captain to pick me
        } finally {
            if (!captain) boarding.unlock();      // the captain keeps the lock until it has driven
        }

        try {
            if (!captain) (party == Party.BLUE ? blueQueue : redQueue).acquire();
            car.seated(party, rider);
            allSeated.await();                    // all 4 seated before anyone drives
            if (captain) car.drive(rider);
        } finally {
            if (captain) boarding.unlock();
        }
    }

    private void take(Party captainParty, int sameParty, int otherParty) {
        if (captainParty == Party.BLUE) { blueWaiting -= sameParty; redWaiting -= otherParty; }
        else                            { redWaiting -= sameParty;  blueWaiting -= otherParty; }
    }
}
