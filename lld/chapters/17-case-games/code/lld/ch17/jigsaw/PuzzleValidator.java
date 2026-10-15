package lld.ch17.jigsaw;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Cheap checks on a box of pieces before anyone tries to solve it. They are necessary, not sufficient:
 * a set that passes can still be unsolvable, but a set that fails can never be solved.
 */
public final class PuzzleValidator {
    private PuzzleValidator() {}

    public static List<String> validate(Puzzle p) {
        List<String> problems = new ArrayList<>();
        int rows = p.rows(), cols = p.cols();
        if (rows < 2 || cols < 2) { problems.add("puzzle must be at least 2 x 2"); return problems; }

        int expected = rows * cols;
        if (p.pieces().size() != expected)
            problems.add("expected " + expected + " pieces, found " + p.pieces().size());

        Set<Integer> ids = new HashSet<>();
        int corners = 0, borders = 0, interior = 0;
        Map<Integer, int[]> tabsAndBlanks = new TreeMap<>();      // shape -> {tabs, blanks}
        for (Piece piece : p.pieces()) {
            if (!ids.add(piece.id())) problems.add("duplicate piece id " + piece.id());
            switch (piece.kind()) {
                case CORNER -> corners++;
                case BORDER -> borders++;
                case INTERIOR -> interior++;
            }
            for (int i = 0; i < 4; i++) {
                Side s = piece.side(i);
                if (s.isFlat()) continue;
                tabsAndBlanks.computeIfAbsent(s.shape(), k -> new int[2])[s.type() == SideType.TAB ? 0 : 1]++;
            }
        }
        int expBorders = 2 * (rows - 2) + 2 * (cols - 2), expInterior = (rows - 2) * (cols - 2);
        if (corners != 4) problems.add("expected 4 corner pieces, found " + corners);
        if (borders != expBorders) problems.add("expected " + expBorders + " border pieces, found " + borders);
        if (interior != expInterior) problems.add("expected " + expInterior + " interior pieces, found " + interior);
        tabsAndBlanks.forEach((shape, tb) -> {
            if (tb[0] != tb[1]) problems.add("shape " + shape + ": " + tb[0] + " tabs but " + tb[1] + " blanks");
        });
        return problems;
    }
}
