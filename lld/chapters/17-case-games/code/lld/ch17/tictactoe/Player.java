package lld.ch17.tictactoe;

public record Player(String name, char mark, MoveStrategy strategy) {}
