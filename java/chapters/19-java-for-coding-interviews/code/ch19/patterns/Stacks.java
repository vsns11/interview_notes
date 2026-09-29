package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Stack for matching/nesting; monotonic stack for "next greater"; monotonic deque for window max. */
public class Stacks {
    static boolean validBrackets(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '(' -> stack.push(')');           // push the EXPECTED closer
                case '[' -> stack.push(']');
                case '{' -> stack.push('}');
                default -> {
                    if (stack.isEmpty() || stack.pop() != c) return false;
                }
            }
        }
        return stack.isEmpty();
    }

    /** For each day, how many days until a warmer one (0 if none). Stack holds indexes, temps decreasing. */
    static int[] daysUntilWarmer(int[] t) {
        int[] ans = new int[t.length];
        Deque<Integer> stack = new ArrayDeque<>();
        for (int i = 0; i < t.length; i++) {
            while (!stack.isEmpty() && t[stack.peek()] < t[i]) {
                int j = stack.pop();
                ans[j] = i - j;                        // i is j's "next greater"
            }
            stack.push(i);
        }
        return ans;
    }

    /** Maximum of every window of size k with a deque of indexes whose values decrease. O(n). */
    static int[] windowMax(int[] a, int k) {
        int[] out = new int[a.length - k + 1];
        Deque<Integer> dq = new ArrayDeque<>();
        for (int i = 0; i < a.length; i++) {
            if (!dq.isEmpty() && dq.peekFirst() <= i - k) dq.pollFirst();       // fell out of window
            while (!dq.isEmpty() && a[dq.peekLast()] <= a[i]) dq.pollLast();    // can never be max again
            dq.offerLast(i);
            if (i >= k - 1) out[i - k + 1] = a[dq.peekFirst()];
        }
        return out;
    }

    /** Evaluate Reverse Polish Notation. */
    static int evalRpn(String... tokens) {
        Deque<Integer> st = new ArrayDeque<>();
        for (String tok : tokens) {
            switch (tok) {
                case "+", "-", "*", "/" -> {
                    int b = st.pop(), a = st.pop();    // order matters for - and /
                    st.push(switch (tok) {
                        case "+" -> a + b;
                        case "-" -> a - b;
                        case "*" -> a * b;
                        default -> a / b;
                    });
                }
                default -> st.push(Integer.parseInt(tok));
            }
        }
        return st.pop();
    }

    public static void main(String[] args) {
        isTrue(validBrackets("{[()]}()"), "brackets ok");
        isTrue(!validBrackets("([)]"), "brackets crossed");
        isTrue(!validBrackets("(("), "brackets unclosed");
        eq(daysUntilWarmer(new int[]{73, 74, 75, 71, 69, 72, 76, 73}), new int[]{1, 1, 4, 2, 1, 1, 0, 0}, "daysUntilWarmer");
        eq(windowMax(new int[]{1, 3, -1, -3, 5, 3, 6, 7}, 3), new int[]{3, 3, 5, 5, 6, 7}, "windowMax");
        eq(evalRpn("4", "13", "5", "/", "+"), 6, "RPN");
        done();
    }
}
