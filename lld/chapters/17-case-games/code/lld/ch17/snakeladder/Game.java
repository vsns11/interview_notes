package lld.ch17.snakeladder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

/** Turn loop: a queue of players, a dice strategy, a board and a rule set. */
public final class Game {
    private final Board board;
    private final Dice dice;
    private final Rules rules;
    private final Deque<Player> turnQueue;
    private final int totalPlayers;
    private final List<Player> finished = new ArrayList<>();
    private final Consumer<String> log;

    public Game(Board board, Dice dice, Rules rules, List<Player> players, Consumer<String> log) {
        if (players.size() < 2) throw new IllegalArgumentException("need at least 2 players");
        this.board = board;
        this.dice = dice;
        this.rules = rules;
        this.turnQueue = new ArrayDeque<>(players);
        this.totalPlayers = players.size();
        this.log = log;
    }

    public boolean isOver() { return rules.win().isGameOver(finished.size(), totalPlayers); }

    /** Plays one player's turn (including bonus rolls). Returns true if the game is now over. */
    public boolean playTurn() {
        if (isOver()) throw new IllegalStateException("game is over");
        Player p = turnQueue.poll();
        takeTurn(p);
        if (p.position() == board.size()) {
            finished.add(p);
            log.accept("  *** " + p.name() + " finishes in place " + finished.size() + " ***");
        } else {
            turnQueue.offer(p);
        }
        return isOver();
    }

    /** Plays until the win strategy says stop. Returns the standings (finishers first, in order). */
    public List<Player> play(int maxTurns) {
        for (int turns = 0; !isOver(); turns++) {
            if (turns == maxTurns) throw new IllegalStateException("no result after " + maxTurns + " turns");
            playTurn();
        }
        return standings();
    }

    public List<Player> standings() {
        List<Player> order = new ArrayList<>(finished);
        order.addAll(turnQueue);          // whoever is still on the board, in turn order
        return order;
    }

    private void takeTurn(Player p) {
        int start = p.position();
        int bonus = 0;
        StringBuilder line = new StringBuilder("  " + p.name() + " @" + start + ":");
        while (true) {
            int roll = dice.roll();
            line.append(" rolls ").append(roll);
            boolean max = rules.sixRollsAgain() && roll == dice.maxRoll();
            if (max && ++bonus == rules.maxBonusRolls()) {
                p.moveTo(start);                       // too many sixes in a row: the whole turn is cancelled
                line.append(" -> ").append(rules.maxBonusRolls()).append(" sixes in a row, turn void, back to ").append(start);
                break;
            }
            move(p, roll, line);
            if (!max || p.position() == board.size()) break;
            line.append(", bonus roll:");
        }
        log.accept(line.toString());
    }

    private void move(Player p, int roll, StringBuilder line) {
        int target = rules.overflow().target(p.position(), roll, board.size());
        if (target == p.position()) { line.append(" -> needs exact roll, stays at ").append(target); return; }
        line.append(" -> ").append(target);
        Jump j = board.jumpAt(target);
        if (j != null) {
            target = j.end();
            line.append(" (").append(j.kind()).append(" to ").append(target).append(')');
        }
        p.moveTo(target);
    }
}
