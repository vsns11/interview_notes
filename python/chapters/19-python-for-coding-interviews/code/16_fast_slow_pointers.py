"""19.16 Fast & slow pointers: cycle start, middle, palindrome list, happy number, duplicate."""
from __future__ import annotations

from dataclasses import dataclass


@dataclass(eq=False)                   # eq=False: compare nodes by identity only
class ListNode:
    val: int
    next: ListNode | None = None


def from_list(values: list[int]) -> ListNode | None:
    dummy = tail = ListNode(0)
    for v in values:
        tail.next = ListNode(v)
        tail = tail.next
    return dummy.next


def cycle_start(head: ListNode | None) -> ListNode | None:
    """Floyd: meet inside the loop, then walk one pointer from head; they meet at the entry."""
    slow = fast = head
    while fast and fast.next:
        slow, fast = slow.next, fast.next.next
        if slow is fast:
            slow = head
            while slow is not fast:
                slow, fast = slow.next, fast.next
            return slow
    return None


def middle(head: ListNode | None) -> ListNode | None:
    """Second middle for even lengths (1-2-3-4 -> 3)."""
    slow = fast = head
    while fast and fast.next:
        slow, fast = slow.next, fast.next.next
    return slow


def is_palindrome(head: ListNode | None) -> bool:
    """O(1) space: find middle, reverse the second half, compare, then restore it."""
    mid = middle(head)
    prev, node = None, mid
    while node:                                    # reverse from mid to the end
        node.next, prev, node = prev, node, node.next
    left, right, ok = head, prev, True
    while right:
        if left.val != right.val:
            ok = False
            break
        left, right = left.next, right.next
    node, back = prev, None                        # restore the input list
    while node:
        node.next, back, node = back, node, node.next
    return ok


def is_happy(n: int) -> bool:
    """Replace n by the sum of squares of its digits; happy if it reaches 1.

    The sequence is an implicit linked list (next = f(n)); it either reaches 1
    or loops, so Floyd detects the loop with O(1) memory instead of a set.
    """
    def f(x: int) -> int:
        return sum(int(d) ** 2 for d in str(x))
    slow, fast = n, f(n)
    while fast != 1 and slow != fast:
        slow, fast = f(slow), f(f(fast))
    return fast == 1


def find_duplicate(nums: list[int]) -> int:
    """n+1 values in 1..n, one repeated, read-only, O(1) space: index -> nums[index] is a
    linked list whose cycle entry is the duplicate."""
    slow = fast = nums[0]
    while True:
        slow, fast = nums[slow], nums[nums[fast]]
        if slow == fast:
            break
    slow = nums[0]
    while slow != fast:
        slow, fast = nums[slow], nums[fast]
    return slow


if __name__ == "__main__":
    head = from_list([3, 2, 0, -4])
    entry = head.next                              # value 2
    head.next.next.next.next = entry               # -4 -> 2 creates the loop
    assert cycle_start(head) is entry
    assert cycle_start(from_list([1, 2, 3])) is None
    print("cycle starts at value:", cycle_start(head).val)

    assert middle(from_list([1, 2, 3, 4, 5])).val == 3
    assert middle(from_list([1, 2, 3, 4])).val == 3

    pal = from_list([1, 2, 3, 2, 1])
    assert is_palindrome(pal) and is_palindrome(from_list([1, 2, 2, 1]))
    assert not is_palindrome(from_list([1, 2, 3])) and is_palindrome(None)
    vals, node = [], pal
    while node:
        vals.append(node.val); node = node.next
    assert vals == [1, 2, 3, 2, 1]                 # list restored after the check
    print("palindrome [1,2,3,2,1]:", is_palindrome(pal))

    assert is_happy(19) and is_happy(1) and not is_happy(2)
    print("happy numbers <= 30:", [n for n in range(1, 31) if is_happy(n)])
    assert find_duplicate([1, 3, 4, 2, 2]) == 2 and find_duplicate([3, 1, 3, 4, 2]) == 3
    print("all asserts passed")
