package ch03.loops;

/** break, continue, labels, and loop-and-a-half. */
public class Branching {
    public static void main(String[] args) {
        int[] data = {4, 8, -1, 15, 16};
        for (int v : data) {
            if (v < 0) {
                System.out.println("found a negative, stopping");
                break;                                 // leave the loop entirely
            }
            System.out.println("processing " + v);
        }

        for (int i = 1; i <= 10; i++) {
            if (i % 3 != 0) continue;                  // skip the rest of this iteration
            System.out.print(i + " ");
        }
        System.out.println("<- multiples of 3");

        int[][] grid = {{1, 2, 3}, {4, 42, 6}, {7, 8, 9}};
        int target = 42;
        int foundRow = -1, foundCol = -1;
        search:                                        // a label names the outer loop
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] == target) {
                    foundRow = r;
                    foundCol = c;
                    break search;                      // exits BOTH loops
                }
            }
        }
        System.out.println(target + " found at (" + foundRow + "," + foundCol + ")");

        outer:
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                if (j == i) continue outer;            // jump to the next i
                System.out.print(i + "" + j + " ");
            }
        }
        System.out.println("<- pairs with j < i");

        String[] lines = {"alpha", "beta", "", "gamma"};
        int idx = 0;
        while (true) {                                  // loop-and-a-half: exit in the middle
            String line = lines[idx++];
            if (line.isEmpty()) break;
            System.out.print(line + " ");
        }
        System.out.println("<- read until blank line");
    }
}
