package lld.ch21.codingblox;

public record Question(int id, Level level, int score) {
    @Override public String toString() { return "Q" + id + "(" + level + "," + score + ")"; }
}
