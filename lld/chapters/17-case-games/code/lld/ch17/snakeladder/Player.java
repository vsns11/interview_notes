package lld.ch17.snakeladder;

public final class Player {
    private final String name;
    private int position;     // 0 = off the board, waiting to enter

    public Player(String name) { this.name = name; }

    public String name() { return name; }
    public int position() { return position; }
    void moveTo(int square) { position = square; }
}
