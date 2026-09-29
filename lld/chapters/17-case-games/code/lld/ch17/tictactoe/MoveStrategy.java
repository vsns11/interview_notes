package lld.ch17.tictactoe;

/** Strategy pattern: human input, scripted test moves, random bot, rule-based bot... all plug in here. */
public interface MoveStrategy {
    Cell nextMove(Board board, int me, int playerCount);
}
