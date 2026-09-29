"""Text preprocessing, tokenization (word / character / a few BPE merges) and a tiny word2vec
(skip-gram with negative sampling) trained in PyTorch on a synthetic corpus."""
import random
import re
from collections import Counter

import torch
from torch import nn

torch.set_num_threads(1)


# ---------------- 1. preprocessing + word tokenization ----------------
def normalize(text):
    text = text.lower()
    text = re.sub(r"https?://\S+", "<url>", text)           # replace URLs with a placeholder
    return re.findall(r"<url>|[a-z0-9]+(?:'[a-z]+)?|[.,!?]", text)


def build_vocab(token_lists, min_count=1):
    counts = Counter(t for toks in token_lists for t in toks)
    itos = ["<pad>", "<unk>"] + sorted(t for t, c in counts.items() if c >= min_count)
    return {t: i for i, t in enumerate(itos)}, itos


def encode(tokens, stoi, max_len):
    ids = [stoi.get(t, stoi["<unk>"]) for t in tokens][:max_len]   # unknown words -> <unk>, truncate
    return ids + [stoi["<pad>"]] * (max_len - len(ids))            # pad to a fixed length


# ---------------- 2. byte-pair encoding: learn a few merges ----------------
def bpe_merges(words, n_merges):
    vocab = Counter({tuple(w) + ("</w>",): c for w, c in Counter(words).items()})
    merges = []
    for _ in range(n_merges):
        pairs = Counter()
        for sym, c in vocab.items():
            for a, b in zip(sym, sym[1:]):
                pairs[(a, b)] += c
        best = max(pairs, key=pairs.get)                          # most frequent adjacent pair
        merges.append(best)
        new_vocab = Counter()
        for sym, c in vocab.items():
            out, i = [], 0
            while i < len(sym):
                if i < len(sym) - 1 and (sym[i], sym[i + 1]) == best:
                    out.append(sym[i] + sym[i + 1]); i += 2
                else:
                    out.append(sym[i]); i += 1
            new_vocab[tuple(out)] += c
        vocab = new_vocab
    return merges, vocab


# ---------------- 3. word2vec skip-gram with negative sampling ----------------
GROUPS = {
    "royal": ["king", "queen", "prince", "princess"],
    "animal": ["dog", "cat", "puppy", "kitten"],
    "fruit": ["apple", "banana", "mango", "cherry"],
}
CONTEXT = {
    "royal": ["rules", "the", "kingdom", "crown", "palace", "throne"],
    "animal": ["chases", "the", "ball", "pet", "barks", "sleeps"],
    "fruit": ["eats", "a", "ripe", "sweet", "juicy", "fresh"],
}


def make_corpus(n_sentences=3000, seed=0):
    rnd = random.Random(seed)
    sents = []
    for _ in range(n_sentences):
        g = rnd.choice(list(GROUPS))
        words = [rnd.choice(GROUPS[g])] + rnd.sample(CONTEXT[g], 4)
        rnd.shuffle(words)
        sents.append(words)
    return sents


class SkipGram(nn.Module):
    def __init__(self, n_words, dim=16):
        super().__init__()
        self.inp = nn.Embedding(n_words, dim)      # the word vectors we keep
        self.ctx = nn.Embedding(n_words, dim)      # separate vectors for words used as context

    def forward(self, center, context, negatives):
        c = self.inp(center)                                               # (B, d)
        pos = (c * self.ctx(context)).sum(-1)                              # score for the real pair
        neg = torch.bmm(self.ctx(negatives), c.unsqueeze(-1)).squeeze(-1)  # scores for random pairs
        return -(nn.functional.logsigmoid(pos) + nn.functional.logsigmoid(-neg).sum(-1)).mean()


def train_word2vec(sents, window=2, epochs=5, k_neg=5):
    stoi, itos = build_vocab(sents)
    pairs = [(stoi[s[i]], stoi[s[j]]) for s in sents for i in range(len(s))
             for j in range(max(0, i - window), min(len(s), i + window + 1)) if i != j]
    pairs = torch.tensor(pairs)
    counts = Counter(t for s in sents for t in s)
    freq = torch.zeros(len(itos))
    for t, c in counts.items():
        freq[stoi[t]] = c ** 0.75                                          # word2vec's noise distribution
    torch.manual_seed(0)
    model = SkipGram(len(itos))
    opt = torch.optim.Adam(model.parameters(), lr=0.01)
    for _ in range(epochs):
        for idx in torch.randperm(len(pairs)).split(512):
            negs = torch.multinomial(freq, len(idx) * k_neg, replacement=True).view(len(idx), k_neg)
            loss = model(pairs[idx, 0], pairs[idx, 1], negs)
            opt.zero_grad(); loss.backward(); opt.step()
    return model.inp.weight.detach(), stoi, itos, loss.item()


def nearest(word, emb, stoi, itos, k=3):
    e = nn.functional.normalize(emb, dim=1)
    sims = e @ e[stoi[word]]
    best = sims.argsort(descending=True)[1:k + 1]
    return [(itos[i], round(sims[i].item(), 2)) for i in best]


if __name__ == "__main__":
    docs = ["The cat sat on the mat!", "Dogs don't like cats, see https://example.com", "The mat is red."]
    toks = [normalize(d) for d in docs]
    for d, t in zip(docs, toks):
        print(f"{d!r:50s} -> {t}")
    stoi, itos = build_vocab(toks)
    print("vocab size", len(itos), "| encode('the dog sat') =", encode(normalize("the dog sat"), stoi, 6))
    print("characters of 'mat':", list("mat"), "| bytes of 'café' (UTF-8):", list("café".encode()))

    words = "low lower lowest newer newest wider widest low low newer".split()
    merges, vocab = bpe_merges(words, 6)
    print("BPE merges learned:", ["".join(m) for m in merges])
    print("words as subword tokens:", sorted({" ".join(s) for s in vocab}))

    sents = make_corpus()
    emb, stoi, itos, final_loss = train_word2vec(sents)
    print(f"word2vec: {len(sents)} sentences, vocab {len(itos)}, final batch loss {final_loss:.3f}")
    for w in ["king", "dog", "mango"]:
        print(f"  nearest to {w!r}: {nearest(w, emb, stoi, itos)}")
    e = nn.functional.normalize(emb, dim=1)
    cos = lambda a, b: (e[stoi[a]] @ e[stoi[b]]).item()
    within = [cos(a, b) for g in GROUPS.values() for a in g for b in g if a < b]
    across = [cos(a, b) for g1 in GROUPS for g2 in GROUPS if g1 < g2 for a in GROUPS[g1] for b in GROUPS[g2]]
    print(f"  mean cosine: same-group nouns {sum(within) / len(within):.2f}  vs  different-group nouns {sum(across) / len(across):.2f}")
