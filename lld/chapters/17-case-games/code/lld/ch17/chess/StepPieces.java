package lld.ch17.chess;

import java.util.ArrayList;
import java.util.List;

/** Shared helper for pieces that jump a fixed set of offsets (king, knight). */
final class StepPieces {
    private StepPieces() {}

    static List<Position> step(Piece piece, Board board, Position from, int[][] offsets) {
        List<Position> out = new ArrayList<>();
        for (int[] d : offsets) {
            int f = from.file() + d[0], r = from.rank() + d[1];
            if (!Position.onBoard(f, r)) continue;
            Position p = new Position(f, r);
            if (piece.canLandOn(board, p)) out.add(p);
        }
        return out;
    }
}
