package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Trie (prefix tree): insert and look up words in O(length of word), independent of word count. */
public class Trie {
    private static final class Node {
        final Node[] next = new Node[26];     // lowercase a-z; use a HashMap<Character, Node> for bigger alphabets
        boolean isWord;
        int prefixCount;                      // how many inserted words pass through this node
    }

    private final Node root = new Node();

    void insert(String word) {
        Node n = root;
        for (char ch : word.toCharArray()) {
            int i = ch - 'a';
            if (n.next[i] == null) n.next[i] = new Node();
            n = n.next[i];
            n.prefixCount++;
        }
        n.isWord = true;
    }

    private Node walk(String s) {
        Node n = root;
        for (char ch : s.toCharArray()) {
            n = n.next[ch - 'a'];
            if (n == null) return null;
        }
        return n;
    }

    boolean contains(String word) { Node n = walk(word); return n != null && n.isWord; }

    boolean startsWith(String prefix) { return walk(prefix) != null; }

    int countWithPrefix(String prefix) { Node n = walk(prefix); return n == null ? 0 : n.prefixCount; }

    /** Autocomplete: all words under a prefix, in alphabetical order (DFS visits children a..z). */
    List<String> complete(String prefix) {
        List<String> out = new ArrayList<>();
        Node n = walk(prefix);
        if (n != null) collect(n, new StringBuilder(prefix), out);
        return out;
    }

    private void collect(Node n, StringBuilder sb, List<String> out) {
        if (n.isWord) out.add(sb.toString());
        for (int i = 0; i < 26; i++) {
            if (n.next[i] == null) continue;
            sb.append((char) ('a' + i));
            collect(n.next[i], sb, out);
            sb.deleteCharAt(sb.length() - 1);   // backtrack
        }
    }

    public static void main(String[] args) {
        Trie t = new Trie();
        for (String w : List.of("car", "card", "care", "cat", "dog")) t.insert(w);
        isTrue(t.contains("car"), "contains car");
        isTrue(!t.contains("ca"), "ca is only a prefix");
        isTrue(t.startsWith("ca"), "startsWith ca");
        eq(t.countWithPrefix("car"), 3, "count prefix car");
        eq(t.complete("car"), List.of("car", "card", "care"), "autocomplete car");
        eq(t.complete("x"), List.of(), "autocomplete none");
        done();
    }
}
