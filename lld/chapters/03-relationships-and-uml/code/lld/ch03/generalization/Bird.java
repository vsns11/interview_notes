package lld.ch03.generalization;

public final class Bird {
    private final String species;
    private MovementStyle movement;   // held, so it can be swapped at run time

    public Bird(String species, MovementStyle movement) {
        this.species = species;
        this.movement = movement;
    }

    public void setMovement(MovementStyle m) { this.movement = m; }
    public String move() { return species + " " + movement.move(); }
}
