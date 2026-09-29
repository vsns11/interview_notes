package lld.ch17.chess;

/** A move as data (the Command's payload). promotion is null unless type == PROMOTION. */
public record Move(Position from, Position to, MoveType type, PieceType promotion) {
    public static Move normal(Position from, Position to) { return new Move(from, to, MoveType.NORMAL, null); }
}
