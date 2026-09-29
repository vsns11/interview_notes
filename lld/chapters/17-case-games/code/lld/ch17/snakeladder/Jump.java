package lld.ch17.snakeladder;

/** A snake (goes down) or a ladder (goes up). Both are just "land on start -> move to end". */
public record Jump(int start, int end) {
    public boolean isSnake() { return end < start; }
    public String kind() { return isSnake() ? "snake" : "ladder"; }
}
