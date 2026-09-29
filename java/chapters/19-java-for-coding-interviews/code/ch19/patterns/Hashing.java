package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Hashing: trade O(n) memory for O(1) average lookups. */
public class Hashing {
    /** Two Sum on UNSORTED input: remember value -> index of everything seen so far. */
    static int[] twoSum(int[] a, int target) {
        Map<Integer, Integer> indexOf = new HashMap<>();
        for (int i = 0; i < a.length; i++) {
            Integer j = indexOf.get(target - a[i]);
            if (j != null) return new int[]{j, i};
            indexOf.put(a[i], i);
        }
        return new int[0];
    }

    /** Group anagrams: the sorted letters are the key. */
    static List<List<String>> groupAnagrams(String[] words) {
        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (String w : words) {
            char[] cs = w.toCharArray();
            Arrays.sort(cs);
            groups.computeIfAbsent(new String(cs), k -> new ArrayList<>()).add(w);
        }
        return new ArrayList<>(groups.values());
    }

    /** Longest run of consecutive integers in O(n): only start counting at a run's first number. */
    static int longestConsecutive(int[] a) {
        Set<Integer> set = new HashSet<>();
        for (int x : a) set.add(x);
        int best = 0;
        for (int x : set) {
            if (set.contains(x - 1)) continue;        // not the start of a run
            int len = 1;
            while (set.contains(x + len)) len++;
            best = Math.max(best, len);
        }
        return best;
    }

    /** First character that appears exactly once, or '_' . */
    static char firstUnique(String s) {
        Map<Character, Integer> count = new LinkedHashMap<>();
        for (char c : s.toCharArray()) count.merge(c, 1, Integer::sum);
        for (var e : count.entrySet()) if (e.getValue() == 1) return e.getKey();
        return '_';
    }

    public static void main(String[] args) {
        eq(twoSum(new int[]{3, 9, 2, 7}, 9), new int[]{2, 3}, "twoSum");
        eq(groupAnagrams(new String[]{"eat", "tea", "tan", "ate", "nat", "bat"}).toString(),
                "[[eat, tea, ate], [tan, nat], [bat]]", "groupAnagrams");
        eq(longestConsecutive(new int[]{100, 4, 200, 1, 3, 2}), 4, "longestConsecutive");
        eq(firstUnique("swiss"), 'w', "firstUnique");
        done();
    }
}
