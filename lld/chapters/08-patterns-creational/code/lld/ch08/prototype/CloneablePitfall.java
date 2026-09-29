package lld.ch08.prototype;

import java.util.ArrayList;
import java.util.List;

/** Why Cloneable is risky: Object.clone() is shallow, so mutable fields are shared. */
class ShallowPlaylist implements Cloneable {
    String name;
    List<String> songs = new ArrayList<>();

    @Override
    public ShallowPlaylist clone() {
        try {
            return (ShallowPlaylist) super.clone();       // field-by-field copy: 'songs' reference is shared
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);                  // cannot happen: we implement Cloneable
        }
    }
}

/** The fixed clone(): deep-copy each mutable field by hand. (It cannot do this for a final field.) */
class DeepPlaylist implements Cloneable {
    String name;
    List<String> songs = new ArrayList<>();

    @Override
    public DeepPlaylist clone() {
        try {
            DeepPlaylist copy = (DeepPlaylist) super.clone();
            copy.songs = new ArrayList<>(songs);          // re-assigning is why 'songs' cannot be final
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
