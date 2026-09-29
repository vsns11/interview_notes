package lld.ch17.game2048;

/**
 * The heart of 2048, written for ONE line moving toward index 0.
 * Every direction is reduced to this by reading the line in the right order.
 * Rule: slide tiles together, merge equal neighbours once per move, the merge nearest the wall first.
 */
public final class LineMerger {
    private LineMerger() {}

    /** Result: the new line and the points earned (sum of merged tile values). */
    public record Result(int[] line, int points) {}

    public static Result merge(int[] in) {
        int n = in.length;
        int[] out = new int[n];
        int write = 0, points = 0;
        int pending = 0;                        // tile waiting for a possible merge partner
        for (int v : in) {
            if (v == 0) continue;
            if (pending == 0) {
                pending = v;
            } else if (pending == v) {
                out[write++] = v * 2;           // merge, and the new tile cannot merge again this move
                points += v * 2;
                pending = 0;
            } else {
                out[write++] = pending;
                pending = v;
            }
        }
        if (pending != 0) out[write] = pending;
        return new Result(out, points);
    }
}
