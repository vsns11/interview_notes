package lld.ch02.composition;

import java.util.Collection;
import java.util.Set;

/**
 * FIXED with composition: HAS-A Set and forwards to it. Works with ANY Set implementation
 * and does not care how that set implements addAll internally.
 */
public class CountingSet<E> {
    private final Set<E> inner;
    private int attempts = 0;

    public CountingSet(Set<E> inner) { this.inner = inner; }

    public boolean add(E e) { attempts++; return inner.add(e); }

    public boolean addAll(Collection<? extends E> c) {
        attempts += c.size();
        boolean changed = false;
        for (E e : c) changed |= inner.add(e);     // talk to the inner set, not to our own add()
        return changed;
    }

    public int size()     { return inner.size(); }
    public int attempts() { return attempts; }
}
