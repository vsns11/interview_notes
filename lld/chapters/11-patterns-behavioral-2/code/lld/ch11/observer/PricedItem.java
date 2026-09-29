package lld.ch11.observer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** The JDK's recommended replacement for java.util.Observable: java.beans.PropertyChangeSupport. */
public final class PricedItem {
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final String name;
    private int priceCents;

    public PricedItem(String name, int priceCents) {
        this.name = name;
        this.priceCents = priceCents;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) { support.addPropertyChangeListener(l); }
    public void removePropertyChangeListener(PropertyChangeListener l) { support.removePropertyChangeListener(l); }

    public void setPriceCents(int newPrice) {
        int old = this.priceCents;
        this.priceCents = newPrice;
        support.firePropertyChange("priceCents", old, newPrice);   // no event if old == new
    }

    public String name() { return name; }
}
