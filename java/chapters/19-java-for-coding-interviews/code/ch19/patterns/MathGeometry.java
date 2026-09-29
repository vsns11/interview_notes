package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Math and geometry: spiral order, reverse an int with overflow check, max points on a line, Josephus. */
public class MathGeometry {
    /** Spiral: shrink four boundaries after each side is walked. */
    static List<Integer> spiral(int[][] m) {
        List<Integer> out = new ArrayList<>();
        if (m.length == 0) return out;
        int top = 0, bottom = m.length - 1, left = 0, right = m[0].length - 1;
        while (top <= bottom && left <= right) {
            for (int c = left; c <= right; c++) out.add(m[top][c]);
            top++;
            for (int r = top; r <= bottom; r++) out.add(m[r][right]);
            right--;
            if (top <= bottom) { for (int c = right; c >= left; c--) out.add(m[bottom][c]); bottom--; }
            if (left <= right) { for (int r = bottom; r >= top; r--) out.add(m[r][left]); left++; }
        }
        return out;
    }

    /** Reverse the digits; return 0 if the result does not fit in an int. Check BEFORE multiplying. */
    static int reverse(int x) {
        int result = 0;
        while (x != 0) {
            int digit = x % 10;                    // negative for negative x: sign is carried along for free
            x /= 10;
            if (result > Integer.MAX_VALUE / 10 || result < Integer.MIN_VALUE / 10) return 0;
            result = result * 10 + digit;          // last digit of a 10-digit int is 1 or 2, so no overflow here
        }
        return result;
    }

    /** Max points on one line: for each anchor, group other points by reduced slope (dy/g, dx/g) with a sign rule. */
    static int maxPointsOnLine(int[][] pts) {
        int best = Math.min(pts.length, 1);
        for (int i = 0; i < pts.length; i++) {
            Map<Long, Integer> slopes = new HashMap<>();
            for (int j = i + 1; j < pts.length; j++) {
                int dx = pts[j][0] - pts[i][0], dy = pts[j][1] - pts[i][1];
                int g = gcd(Math.abs(dx), Math.abs(dy));
                dx /= g; dy /= g;
                if (dx < 0 || (dx == 0 && dy < 0)) { dx = -dx; dy = -dy; }   // (1,2) and (-1,-2) are the same line
                long key = ((long) dx << 32) ^ (dy & 0xFFFFFFFFL);          // exact key, no double rounding
                best = Math.max(best, 1 + slopes.merge(key, 1, Integer::sum));
            }
        }
        return best;
    }

    private static int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }

    /** Josephus: n people in a circle, every k-th leaves; 0-based survivor. J(1) = 0, J(n) = (J(n-1) + k) mod n. */
    static int josephus(int n, int k) {
        int survivor = 0;
        for (int size = 2; size <= n; size++) survivor = (survivor + k) % size;
        return survivor;
    }

    public static void main(String[] args) {
        eq(spiral(new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}}), List.of(1, 2, 3, 6, 9, 8, 7, 4, 5), "spiral 3x3");
        eq(spiral(new int[][]{{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}}), List.of(1, 2, 3, 4, 8, 12, 11, 10, 9, 5, 6, 7), "spiral 3x4");
        eq(spiral(new int[][]{{1}, {2}, {3}}), List.of(1, 2, 3), "spiral one column");
        eq(reverse(123), 321, "reverse 123");
        eq(reverse(-120), -21, "reverse -120");
        eq(reverse(1534236469), 0, "reverse 1534236469 overflows");
        eq(reverse(-2147483412), -2143847412, "reverse near MIN_VALUE fits");
        eq(maxPointsOnLine(new int[][]{{1, 1}, {2, 2}, {3, 3}}), 3, "three collinear");
        eq(maxPointsOnLine(new int[][]{{1, 1}, {3, 2}, {5, 3}, {4, 1}, {2, 3}, {1, 4}}), 4, "four on one line");
        eq(maxPointsOnLine(new int[][]{{0, 0}, {0, 5}, {0, -2}, {1, 1}}), 3, "vertical line");
        eq(josephus(7, 3), 3, "Josephus n=7 k=3 (0-based)");
        eq(josephus(1, 5), 0, "Josephus single person");
        done();
    }
}
