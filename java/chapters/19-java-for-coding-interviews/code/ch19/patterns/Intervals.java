package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Intervals: sort by start, then sweep. */
public class Intervals {
    static int[][] merge(int[][] intervals) {
        int[][] sorted = intervals.clone();
        Arrays.sort(sorted, Comparator.comparingInt(x -> x[0]));
        List<int[]> out = new ArrayList<>();
        for (int[] cur : sorted) {
            if (!out.isEmpty() && cur[0] <= out.getLast()[1]) {          // overlaps the last merged one
                out.getLast()[1] = Math.max(out.getLast()[1], cur[1]);
            } else {
                out.add(new int[]{cur[0], cur[1]});
            }
        }
        return out.toArray(new int[0][]);
    }

    /** Insert into a sorted, non-overlapping list: copy before, merge the overlap, copy after. */
    static int[][] insert(int[][] sorted, int[] add) {
        List<int[]> out = new ArrayList<>();
        int i = 0, n = sorted.length;
        int start = add[0], end = add[1];
        while (i < n && sorted[i][1] < start) out.add(sorted[i++]);
        while (i < n && sorted[i][0] <= end) {
            start = Math.min(start, sorted[i][0]);
            end = Math.max(end, sorted[i][1]);
            i++;
        }
        out.add(new int[]{start, end});
        while (i < n) out.add(sorted[i++]);
        return out.toArray(new int[0][]);
    }

    /** Minimum meeting rooms: min-heap of end times of meetings in progress. [start, end) semantics. */
    static int minRooms(int[][] meetings) {
        int[][] sorted = meetings.clone();
        Arrays.sort(sorted, Comparator.comparingInt(x -> x[0]));
        PriorityQueue<Integer> ends = new PriorityQueue<>();
        for (int[] m : sorted) {
            if (!ends.isEmpty() && ends.peek() <= m[0]) ends.poll();    // reuse the room that freed first
            ends.offer(m[1]);
        }
        return ends.size();
    }

    public static void main(String[] args) {
        eq(merge(new int[][]{{8, 10}, {1, 3}, {2, 6}, {15, 18}}), new int[][]{{1, 6}, {8, 10}, {15, 18}}, "merge");
        eq(merge(new int[][]{{1, 4}, {4, 5}}), new int[][]{{1, 5}}, "merge touching");
        eq(insert(new int[][]{{1, 2}, {3, 5}, {6, 7}, {8, 10}}, new int[]{4, 8}), new int[][]{{1, 2}, {3, 10}}, "insert");
        eq(minRooms(new int[][]{{0, 30}, {5, 10}, {15, 20}}), 2, "minRooms");
        eq(minRooms(new int[][]{{1, 5}, {5, 8}}), 1, "minRooms back-to-back");
        done();
    }
}
