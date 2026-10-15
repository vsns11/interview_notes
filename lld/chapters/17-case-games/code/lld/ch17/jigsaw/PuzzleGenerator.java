package lld.ch17.jigsaw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Cuts a rows x cols picture into pieces, then turns and shuffles them. The Random is injected, so the
 * same seed always produces the same box of pieces.
 * shapeVariety = 0 gives every joint its own shape code (one solution only);
 * shapeVariety = k reuses k codes, so different pieces can look alike and solvers must backtrack.
 */
public final class PuzzleGenerator {
    private final Random rng;
    private int nextShape = 1;

    public PuzzleGenerator(Random rng) { this.rng = rng; }

    public Puzzle generate(int rows, int cols, int shapeVariety) {
        Side[][][] sides = new Side[rows][cols][4];
        for (Side[][] row : sides) for (Side[] cell : row) java.util.Arrays.fill(cell, Side.FLAT);

        for (int r = 0; r < rows; r++)                         // vertical joints: cell and its right neighbour
            for (int c = 0; c + 1 < cols; c++) {
                Side s = cut(shapeVariety);
                sides[r][c][Direction.RIGHT.ordinal()] = s;
                sides[r][c + 1][Direction.LEFT.ordinal()] = s.partner();
            }
        for (int r = 0; r + 1 < rows; r++)                     // horizontal joints: cell and the one below
            for (int c = 0; c < cols; c++) {
                Side s = cut(shapeVariety);
                sides[r][c][Direction.BOTTOM.ordinal()] = s;
                sides[r + 1][c][Direction.TOP.ordinal()] = s.partner();
            }

        List<Side[]> loose = new ArrayList<>();
        for (Side[][] row : sides) for (Side[] cell : row) loose.add(cell);
        Collections.shuffle(loose, rng);                       // jumble the order...
        List<Piece> pieces = new ArrayList<>();
        for (int i = 0; i < loose.size(); i++) {
            Side[] s = loose.get(i);
            int turn = rng.nextInt(4);                         // ...and turn each piece at random
            Side[] t = new Side[4];
            for (int j = 0; j < 4; j++) t[(j + turn) % 4] = s[j];
            pieces.add(new Piece(i + 1, t[0], t[1], t[2], t[3]));
        }
        return new Puzzle(rows, cols, pieces);
    }

    private Side cut(int shapeVariety) {
        int shape = shapeVariety <= 0 ? nextShape++ : 1 + rng.nextInt(shapeVariety);
        return rng.nextBoolean() ? Side.tab(shape) : Side.blank(shape);
    }
}
