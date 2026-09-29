"""19.7 Linked lists and binary trees as small Python classes."""
from __future__ import annotations

from collections import deque
from dataclasses import dataclass


# ---------------- linked list ----------------
@dataclass
class ListNode:
    val: int
    next: ListNode | None = None


def from_list(values: list[int]) -> ListNode | None:
    dummy = tail = ListNode(0)          # dummy head avoids special cases
    for v in values:
        tail.next = ListNode(v)
        tail = tail.next
    return dummy.next


def to_list(head: ListNode | None) -> list[int]:
    out = []
    while head:
        out.append(head.val)
        head = head.next
    return out


def reverse(head: ListNode | None) -> ListNode | None:
    prev = None
    while head:
        head.next, prev, head = prev, head, head.next   # RHS evaluated first
    return prev


def middle(head: ListNode | None) -> ListNode | None:
    slow = fast = head
    while fast and fast.next:
        slow, fast = slow.next, fast.next.next
    return slow


def has_cycle(head: ListNode | None) -> bool:
    slow = fast = head
    while fast and fast.next:
        slow, fast = slow.next, fast.next.next
        if slow is fast:                 # identity, not equality
            return True
    return False


def merge_two(a: ListNode | None, b: ListNode | None) -> ListNode | None:
    dummy = tail = ListNode(0)
    while a and b:
        if a.val <= b.val:
            tail.next, a = a, a.next
        else:
            tail.next, b = b, b.next
        tail = tail.next
    tail.next = a or b
    return dummy.next


# ---------------- binary tree ----------------
@dataclass
class TreeNode:
    val: int
    left: TreeNode | None = None
    right: TreeNode | None = None


def build_tree(level_order: list[int | None]) -> TreeNode | None:
    """LeetCode-style list: [3, 9, 20, None, None, 15, 7]."""
    if not level_order or level_order[0] is None:
        return None
    root = TreeNode(level_order[0])
    q = deque([root])
    i = 1
    while q and i < len(level_order):
        node = q.popleft()
        for side in ("left", "right"):
            if i < len(level_order) and level_order[i] is not None:
                child = TreeNode(level_order[i])
                setattr(node, side, child)
                q.append(child)
            i += 1
    return root


def inorder_recursive(root: TreeNode | None) -> list[int]:
    if not root:
        return []
    return inorder_recursive(root.left) + [root.val] + inorder_recursive(root.right)


def inorder_iterative(root: TreeNode | None) -> list[int]:
    out, stack, node = [], [], root
    while stack or node:
        while node:                 # go left as far as possible
            stack.append(node)
            node = node.left
        node = stack.pop()
        out.append(node.val)
        node = node.right
    return out


def preorder_iterative(root: TreeNode | None) -> list[int]:
    out, stack = [], [root] if root else []
    while stack:
        node = stack.pop()
        out.append(node.val)
        if node.right:
            stack.append(node.right)   # push right first so left pops first
        if node.left:
            stack.append(node.left)
    return out


def level_order(root: TreeNode | None) -> list[list[int]]:
    if not root:
        return []
    levels, q = [], deque([root])
    while q:
        levels.append([])
        for _ in range(len(q)):        # exactly one level per outer loop
            node = q.popleft()
            levels[-1].append(node.val)
            q.extend(c for c in (node.left, node.right) if c)
    return levels


def max_depth(root: TreeNode | None) -> int:
    return 0 if not root else 1 + max(max_depth(root.left), max_depth(root.right))


def is_valid_bst(root: TreeNode | None, lo=float("-inf"), hi=float("inf")) -> bool:
    if not root:
        return True
    if not lo < root.val < hi:
        return False
    return is_valid_bst(root.left, lo, root.val) and is_valid_bst(root.right, root.val, hi)


def lca(root: TreeNode | None, p: int, q: int) -> TreeNode | None:
    """Lowest common ancestor in a general binary tree."""
    if root is None or root.val in (p, q):
        return root
    left, right = lca(root.left, p, q), lca(root.right, p, q)
    return root if left and right else left or right


def serialize(root: TreeNode | None) -> str:
    """Pre-order with '#' for missing children: every node is recoverable."""
    out: list[str] = []
    stack = [root]
    while stack:                       # iterative pre-order: no recursion limit
        node = stack.pop()
        if node is None:
            out.append("#")
            continue
        out.append(str(node.val))
        stack.append(node.right)       # right pushed first so left is popped first
        stack.append(node.left)
    return ",".join(out)


def deserialize(data: str) -> TreeNode | None:
    tokens = iter(data.split(","))

    def build() -> TreeNode | None:
        tok = next(tokens)
        if tok == "#":
            return None
        node = TreeNode(int(tok))
        node.left = build()            # consumes exactly the left subtree's tokens
        node.right = build()
        return node

    return build()


if __name__ == "__main__":
    head = from_list([1, 2, 3, 4, 5])
    assert middle(head).val == 3
    head = reverse(head)
    assert to_list(head) == [5, 4, 3, 2, 1]
    assert to_list(merge_two(from_list([1, 2, 4]), from_list([1, 3, 4]))) == [1, 1, 2, 3, 4, 4]
    cyc = from_list([1, 2, 3])
    cyc.next.next.next = cyc.next              # 3 -> 2 makes a loop
    assert has_cycle(cyc) and not has_cycle(from_list([1, 2]))
    print("reversed list:", to_list(head))

    root = build_tree([3, 9, 20, None, None, 15, 7])
    print("inorder:", inorder_iterative(root), "preorder:", preorder_iterative(root))
    print("levels:", level_order(root), "depth:", max_depth(root))
    assert inorder_recursive(root) == inorder_iterative(root) == [9, 3, 15, 20, 7]
    assert level_order(root) == [[3], [9, 20], [15, 7]]
    assert max_depth(root) == 3
    assert is_valid_bst(build_tree([2, 1, 3]))
    assert not is_valid_bst(build_tree([5, 1, 4, None, None, 3, 6]))
    t = build_tree([3, 5, 1, 6, 2, 0, 8, None, None, 7, 4])
    assert lca(t, 5, 1).val == 3 and lca(t, 5, 4).val == 5
    s = serialize(root)
    print("serialized:", s)
    assert s == "3,9,#,#,20,15,#,#,7,#,#"
    assert deserialize(s) == root                  # dataclass __eq__ compares structure
    assert serialize(deserialize(serialize(t))) == serialize(t)
    assert serialize(None) == "#" and deserialize("#") is None
    print("all asserts passed")
