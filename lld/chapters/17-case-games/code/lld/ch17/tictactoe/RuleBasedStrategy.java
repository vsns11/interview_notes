package lld.ch17.tictactoe;

import java.util.List;

/** Win if you can, else block an opponent's win, else take the centre, else the first corner, else anything. */
public final class RuleBasedStrategy implements MoveStrategy {
    @Override public Cell nextMove(Board board, int me, int playerCount) {
        List<Cell> empty = board.emptyCells();
        for (Cell c : empty) if (board.wouldWin(c, me)) return c;
        for (int other = 0; other < playerCount; other++) {
            if (other == me) continue;
            for (Cell c : empty) if (board.wouldWin(c, other)) return c;
        }
        int n = board.size();
        Cell centre = new Cell(n / 2, n / 2);
        if (board.isEmpty(centre)) return centre;
        for (Cell corner : List.of(new Cell(0, 0), new Cell(0, n - 1), new Cell(n - 1, 0), new Cell(n - 1, n - 1))) {
            if (board.isEmpty(corner)) return corner;
        }
        return empty.get(0);
    }
}
