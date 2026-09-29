package lld.ch08.singleton;

import java.io.Serializable;

/** A parent that (unwisely) exposes a public clone(). Children inherit it. */
class CloneableBase implements Cloneable {
    @Override
    public Object clone() throws CloneNotSupportedException { return super.clone(); }
}

/** A classic eager singleton with NO defences. Reflection, serialization and cloning all break it. */
public final class BreakableSingleton extends CloneableBase implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final BreakableSingleton INSTANCE = new BreakableSingleton();

    private BreakableSingleton() { }
}
