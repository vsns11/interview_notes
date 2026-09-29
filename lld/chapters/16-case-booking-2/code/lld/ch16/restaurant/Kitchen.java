package lld.ch16.restaurant;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

final class Chef extends Employee {
    private final Deque<Order> queue = new ArrayDeque<>();
    private Order current;

    Chef(String id, String name) { super(id, name); }

    synchronized void enqueue(Order o) { queue.addLast(o); }
    synchronized int load() { return queue.size() + (current == null ? 0 : 1); }

    /** Take the next order in FIFO order and start cooking it. */
    synchronized Order startNext() {
        if (current != null) throw new IllegalStateException(name() + " is still cooking " + current.id());
        current = queue.pollFirst();
        if (current != null) current.moveTo(OrderStatus.PREPARING);
        return current;
    }

    synchronized void finishCurrent() {
        if (current == null) throw new IllegalStateException(name() + " has nothing on the stove");
        current.moveTo(OrderStatus.READY);
        current = null;
    }
}

/** Strategy: which chef gets the next order. */
interface ChefAssignment { Chef pick(List<Chef> chefs); }

final class RoundRobinAssignment implements ChefAssignment {
    private int next;
    public synchronized Chef pick(List<Chef> chefs) { return chefs.get(next++ % chefs.size()); }
}

final class LeastLoadedAssignment implements ChefAssignment {
    public Chef pick(List<Chef> chefs) { return chefs.stream().min(Comparator.comparingInt(Chef::load)).orElseThrow(); }
}

final class Kitchen {
    private final List<Chef> chefs = new CopyOnWriteArrayList<>();
    private final ChefAssignment assignment;

    Kitchen(ChefAssignment assignment) { this.assignment = assignment; }
    void addChef(Chef c) { chefs.add(c); }

    Chef submit(Order o) {
        if (chefs.isEmpty()) throw new IllegalStateException("no chef on shift");
        Chef c = assignment.pick(chefs);
        c.enqueue(o);
        return c;
    }
}
