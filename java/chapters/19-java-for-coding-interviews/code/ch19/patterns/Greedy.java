package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Greedy: jump game (reach and fewest jumps), gas station, candies. */
public class Greedy {
    /** Can you reach the last index? Track the farthest index reachable so far. */
    static boolean canJump(int[] jumps) {
        int farthest = 0;
        for (int i = 0; i < jumps.length; i++) {
            if (i > farthest) return false;                    // stuck: i is beyond every reachable index
            farthest = Math.max(farthest, i + jumps[i]);
        }
        return true;
    }

    /** Fewest jumps: an implicit BFS — [start, end] is the range reachable with `jumps` jumps. */
    static int minJumps(int[] jumps) {
        int count = 0, end = 0, farthest = 0;
        for (int i = 0; i < jumps.length - 1; i++) {
            farthest = Math.max(farthest, i + jumps[i]);
            if (i == end) { count++; end = farthest; }         // the current range is used up: jump
        }
        return count;
    }

    /** Gas station: if total gas >= total cost there is a start; it is just after the last point the tank went negative. */
    static int gasStation(int[] gas, int[] cost) {
        int total = 0, tank = 0, start = 0;
        for (int i = 0; i < gas.length; i++) {
            int diff = gas[i] - cost[i];
            total += diff;
            tank += diff;
            if (tank < 0) { start = i + 1; tank = 0; }        // no station in [start..i] can work
        }
        return total >= 0 ? start : -1;
    }

    /** Candies: each child >= 1; a higher rating than a neighbour means more candy. Two passes, one per direction. */
    static int candies(int[] ratings) {
        int n = ratings.length;
        int[] c = new int[n];
        Arrays.fill(c, 1);
        for (int i = 1; i < n; i++)                            // left neighbour rule
            if (ratings[i] > ratings[i - 1]) c[i] = c[i - 1] + 1;
        for (int i = n - 2; i >= 0; i--)                       // right neighbour rule, keep the left result if larger
            if (ratings[i] > ratings[i + 1]) c[i] = Math.max(c[i], c[i + 1] + 1);
        int sum = 0;
        for (int x : c) sum += x;
        return sum;
    }

    public static void main(String[] args) {
        eq(canJump(new int[]{2, 3, 1, 1, 4}), true, "canJump yes");
        eq(canJump(new int[]{3, 2, 1, 0, 4}), false, "canJump blocked by 0");
        eq(minJumps(new int[]{2, 3, 1, 1, 4}), 2, "minJumps");
        eq(minJumps(new int[]{0}), 0, "minJumps already there");
        eq(gasStation(new int[]{1, 2, 3, 4, 5}, new int[]{3, 4, 5, 1, 2}), 3, "gas station start");
        eq(gasStation(new int[]{2, 3, 4}, new int[]{3, 4, 3}), -1, "gas station impossible");
        eq(candies(new int[]{1, 0, 2}), 5, "candies 2,1,2");
        eq(candies(new int[]{1, 2, 2}), 4, "candies equal neighbours");
        eq(candies(new int[]{1, 3, 4, 5, 2}), 11, "candies 1,2,3,4,1");
        done();
    }
}
