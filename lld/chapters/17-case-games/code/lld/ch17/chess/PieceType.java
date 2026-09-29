package lld.ch17.chess;

public enum PieceType {
    KING('K'), QUEEN('Q'), ROOK('R'), BISHOP('B'), KNIGHT('N'), PAWN('P');

    private final char letter;

    PieceType(char letter) { this.letter = letter; }

    public char letter() { return letter; }
}
