package ch04.arrays;

import java.util.Arrays;

public class MultiDim {
    public static void main(String[] args) {
        int[][] grid = new int[3][4];                  // 3 rows, each an int[4]
        for (int r = 0; r < grid.length; r++)
            for (int c = 0; c < grid[r].length; c++)
                grid[r][c] = r * 10 + c;
        System.out.println(Arrays.deepToString(grid));

        int[][] jagged = new int[3][];                 // rows allocated separately
        jagged[0] = new int[] {1};
        jagged[1] = new int[] {1, 1};
        jagged[2] = new int[] {1, 2, 1};
        for (int[] row : jagged) System.out.println(Arrays.toString(row) + " len=" + row.length);

        // Pascal's triangle: a natural jagged array
        int n = 6;
        int[][] pascal = new int[n][];
        for (int i = 0; i < n; i++) {
            pascal[i] = new int[i + 1];
            pascal[i][0] = pascal[i][i] = 1;
            for (int j = 1; j < i; j++) pascal[i][j] = pascal[i - 1][j - 1] + pascal[i - 1][j];
        }
        System.out.println("row 5 of Pascal: " + Arrays.toString(pascal[5]));

        char[][] board = {
            {'X', 'O', 'X'},
            {' ', 'X', 'O'},
            {'O', ' ', 'X'},
        };
        boolean diagonal = board[0][0] == 'X' && board[1][1] == 'X' && board[2][2] == 'X';
        System.out.println("X wins on the diagonal? " + diagonal);

        int[][] missingRow = new int[2][];
        System.out.println("unallocated row is " + missingRow[1]);

        int[][] a = {{1, 2}, {3, 4}}, b = {{1, 2}, {3, 4}};
        System.out.println("Arrays.equals: " + Arrays.equals(a, b) + ", Arrays.deepEquals: " + Arrays.deepEquals(a, b));
    }
}
