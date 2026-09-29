package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Tree recursion templates: validate a BST, lowest common ancestor, max path sum, serialize/deserialize. */
public class Trees {
    record TreeNode(int val, TreeNode left, TreeNode right) {
        static TreeNode leaf(int v) { return new TreeNode(v, null, null); }
    }

    /** Top-down template: pass the allowed range DOWN. long bounds so Integer.MIN/MAX values are legal keys. */
    static boolean isValidBst(TreeNode root) {
        return valid(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    private static boolean valid(TreeNode n, long low, long high) {
        if (n == null) return true;
        if (n.val() <= low || n.val() >= high) return false;
        return valid(n.left(), low, n.val()) && valid(n.right(), n.val(), high);
    }

    /** Bottom-up template: each call reports what it found; the first node that sees p and q on different sides is the LCA. */
    static TreeNode lca(TreeNode root, int p, int q) {
        if (root == null || root.val() == p || root.val() == q) return root;
        TreeNode left = lca(root.left(), p, q), right = lca(root.right(), p, q);
        if (left != null && right != null) return root;       // p and q split here
        return left != null ? left : right;                   // pass up whichever side found something
    }

    /** Bottom-up with a side result: return the best DOWNWARD path, record the best path THROUGH each node. */
    static int maxPathSum(TreeNode root) {
        int[] best = {Integer.MIN_VALUE};
        gain(root, best);
        return best[0];
    }

    private static int gain(TreeNode n, int[] best) {
        if (n == null) return 0;
        int left = Math.max(0, gain(n.left(), best));          // a negative branch is better skipped
        int right = Math.max(0, gain(n.right(), best));
        best[0] = Math.max(best[0], n.val() + left + right);   // path that bends at n
        return n.val() + Math.max(left, right);                // a parent can extend only one side
    }

    /** Preorder with "#" for null: the markers make the shape unambiguous, so one traversal is enough. */
    static String serialize(TreeNode root) {
        StringJoiner out = new StringJoiner(",");
        write(root, out);
        return out.toString();
    }

    private static void write(TreeNode n, StringJoiner out) {
        if (n == null) { out.add("#"); return; }
        out.add(String.valueOf(n.val()));
        write(n.left(), out);
        write(n.right(), out);
    }

    static TreeNode deserialize(String data) {
        return read(new ArrayDeque<>(Arrays.asList(data.split(","))));
    }

    private static TreeNode read(Deque<String> tokens) {
        String t = tokens.poll();
        if (t.equals("#")) return null;
        TreeNode left = read(tokens);              // same order as write: value, left, right
        TreeNode right = read(tokens);
        return new TreeNode(Integer.parseInt(t), left, right);
    }

    public static void main(String[] args) {
        TreeNode bst = new TreeNode(5, new TreeNode(3, TreeNode.leaf(2), TreeNode.leaf(4)), new TreeNode(8, null, TreeNode.leaf(9)));
        eq(isValidBst(bst), true, "valid BST");
        TreeNode bad = new TreeNode(5, TreeNode.leaf(3), new TreeNode(8, TreeNode.leaf(4), null)); // 4 is right of 5
        eq(isValidBst(bad), false, "grandchild breaks the BST rule");
        eq(isValidBst(TreeNode.leaf(Integer.MAX_VALUE)), true, "MAX_VALUE key is legal");

        TreeNode t = new TreeNode(3,
                new TreeNode(5, TreeNode.leaf(6), new TreeNode(2, TreeNode.leaf(7), TreeNode.leaf(4))),
                new TreeNode(1, TreeNode.leaf(0), TreeNode.leaf(8)));
        eq(lca(t, 5, 1).val(), 3, "lca(5, 1)");
        eq(lca(t, 7, 4).val(), 2, "lca(7, 4)");
        eq(lca(t, 5, 4).val(), 5, "lca where one is the ancestor");

        eq(maxPathSum(new TreeNode(-10, TreeNode.leaf(9), new TreeNode(20, TreeNode.leaf(15), TreeNode.leaf(7)))), 42, "max path 15-20-7");
        eq(maxPathSum(TreeNode.leaf(-3)), -3, "all negative");

        String s = serialize(t);
        eq(s, "3,5,6,#,#,2,7,#,#,4,#,#,1,0,#,#,8,#,#", "serialize");
        eq(deserialize(s).equals(t), true, "deserialize round trip (records compare by value)");
        eq(deserialize("#"), null, "empty tree");
        done();
    }
}
