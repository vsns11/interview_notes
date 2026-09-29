package lld.ch17.chess;

/** A square. file 0..7 = a..h, rank 0..7 = 1..8. Immutable value object. */
public record Position(int file, int rank) {
    public Position {
        if (!onBoard(file, rank)) throw new IllegalArgumentException("off board: " + file + "," + rank);
    }

    public static boolean onBoard(int file, int rank) { return file >= 0 && file < 8 && rank >= 0 && rank < 8; }

    /** Parses algebraic notation such as "e4". */
    public static Position of(String sq) {
        if (sq == null || sq.length() != 2) throw new IllegalArgumentException("bad square: " + sq);
        return new Position(sq.charAt(0) - 'a', sq.charAt(1) - '1');
    }

    @Override public String toString() { return "" + (char) ('a' + file) + (rank + 1); }
}
