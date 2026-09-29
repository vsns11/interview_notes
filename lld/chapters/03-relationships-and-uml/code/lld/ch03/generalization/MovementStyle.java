package lld.ch03.generalization;

/** "Prefer holding over extending": behaviour that varies is held, not inherited. */
public interface MovementStyle {
    String move();

    MovementStyle FLY = () -> "flies";
    MovementStyle SWIM = () -> "swims";
    MovementStyle WALK = () -> "walks";
}
