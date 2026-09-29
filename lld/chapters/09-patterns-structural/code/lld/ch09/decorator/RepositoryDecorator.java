package lld.ch09.decorator;

/**
 * BASE DECORATOR: holds the INTERFACE (not a concrete class) and forwards everything.
 * Subclasses override only what they change, so no method is ever forgotten.
 */
abstract class RepositoryDecorator implements UserRepository {
    protected final UserRepository inner;

    protected RepositoryDecorator(UserRepository inner) { this.inner = inner; }

    @Override public String findName(int id)          { return inner.findName(id); }
    @Override public void rename(int id, String name) { inner.rename(id, name); }
}
