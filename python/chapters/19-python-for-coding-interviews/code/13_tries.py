"""19.13 Tries (prefix trees): insert/search/prefix, wildcard search, word search on a board."""
from __future__ import annotations


class TrieNode:
    __slots__ = ("children", "word")        # slots: many small nodes, less memory

    def __init__(self):
        self.children: dict[str, TrieNode] = {}
        self.word: str | None = None         # the full word if one ends here


class Trie:
    def __init__(self):
        self.root = TrieNode()

    def insert(self, word: str) -> None:
        node = self.root
        for ch in word:
            node = node.children.setdefault(ch, TrieNode())
        node.word = word

    def _walk(self, s: str) -> TrieNode | None:
        node = self.root
        for ch in s:
            node = node.children.get(ch)
            if node is None:
                return None
        return node

    def search(self, word: str) -> bool:
        node = self._walk(word)
        return node is not None and node.word is not None

    def starts_with(self, prefix: str) -> bool:
        return self._walk(prefix) is not None

    def words_with_prefix(self, prefix: str, limit: int = 3) -> list[str]:
        """Autocomplete: DFS below the prefix node, alphabetical, first `limit` words."""
        start = self._walk(prefix)
        out: list[str] = []
        stack = [start] if start else []
        while stack and len(out) < limit:
            node = stack.pop()
            if node.word is not None:
                out.append(node.word)
            # push children in reverse so the smallest letter is popped first
            stack.extend(node.children[c] for c in sorted(node.children, reverse=True))
        return out

    def search_wildcard(self, pattern: str) -> bool:
        """'.' matches any one letter: branch into every child at a dot."""
        def dfs(node: TrieNode, i: int) -> bool:
            if i == len(pattern):
                return node.word is not None
            ch = pattern[i]
            if ch == ".":
                return any(dfs(child, i + 1) for child in node.children.values())
            child = node.children.get(ch)
            return child is not None and dfs(child, i + 1)
        return dfs(self.root, 0)


def find_words(board: list[list[str]], words: list[str]) -> list[str]:
    """Word search II: one DFS per cell, guided by a trie of all target words.

    The trie lets one walk check every word at once and stop as soon as the path
    is no longer a prefix of any word. Found words are cleared so they are
    reported once, and empty branches are pruned so later walks skip them.
    """
    trie = Trie()
    for w in words:
        trie.insert(w)
    rows, cols = len(board), len(board[0])
    found: list[str] = []

    def dfs(r: int, c: int, parent: TrieNode) -> None:
        ch = board[r][c]
        node = parent.children.get(ch)
        if node is None:
            return
        if node.word is not None:
            found.append(node.word)
            node.word = None                     # report each word once
        board[r][c] = "#"                        # mark visited in place
        for dr, dc in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nr, nc = r + dr, c + dc
            if 0 <= nr < rows and 0 <= nc < cols and board[nr][nc] != "#":
                dfs(nr, nc, node)
        board[r][c] = ch                         # undo (backtracking)
        if not node.children and node.word is None:
            del parent.children[ch]              # prune a finished branch

    for r in range(rows):
        for c in range(cols):
            dfs(r, c, trie.root)
    return sorted(found)


if __name__ == "__main__":
    t = Trie()
    for w in ["car", "card", "care", "cart", "cat", "dog"]:
        t.insert(w)
    assert t.search("car") and not t.search("ca")
    assert t.starts_with("ca") and not t.starts_with("cow")
    print("autocomplete 'car':", t.words_with_prefix("car"))
    assert t.words_with_prefix("car") == ["car", "card", "care"]
    assert t.words_with_prefix("z") == []

    assert t.search_wildcard("c.t") and t.search_wildcard("...") and t.search_wildcard("ca.e")
    assert t.search_wildcard("c..")                          # "car" and "cat" match
    assert not t.search_wildcard(".o") and not t.search_wildcard("....s")
    print("wildcard 'ca.e':", t.search_wildcard("ca.e"), "| '.o':", t.search_wildcard(".o"))

    board = [list("oaan"), list("etae"), list("ihkr"), list("iflv")]
    got = find_words(board, ["oath", "pea", "eat", "rain", "oat", "hike"])
    print("words on board:", got)
    assert got == ["eat", "oat", "oath"]          # "oat" is a prefix of "oath": both found
    assert board[0] == list("oaan")                         # board restored
    print("all asserts passed")
