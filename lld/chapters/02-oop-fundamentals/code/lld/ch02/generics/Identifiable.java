package lld.ch02.generics;

/** Generic interface: ID is a TYPE PARAMETER filled in by each implementer. */
public interface Identifiable<ID> {
    ID id();
}
