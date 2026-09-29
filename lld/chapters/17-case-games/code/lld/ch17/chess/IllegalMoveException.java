package lld.ch17.chess;

public final class IllegalMoveException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public IllegalMoveException(String message) { super(message); }
}
