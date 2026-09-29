package lld.ch01.playlist;

/** The rule that decides which position plays next. A rule that varies becomes its own type. */
public interface PlayOrder {
    int NONE = -1;                           // "there is no next position"

    int first(int size);                     // first position to play, or NONE if empty
    int after(int current, int size);        // position after 'current', or NONE at the end
}
