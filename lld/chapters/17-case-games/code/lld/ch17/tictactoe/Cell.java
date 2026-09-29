package lld.ch17.tictactoe;

public record Cell(int row, int col) {
    @Override public String toString() { return "(" + row + "," + col + ")"; }
}
