"""Next-token prediction in miniature: train a tiny character-level GPT on CPU.

Shows the whole LLM recipe at toy scale: tokenize -> predict next token ->
cross-entropy loss -> generate autoregressively.
"""
import math
import time

import torch
import torch.nn as nn
import torch.nn.functional as F

torch.manual_seed(0)

import random

random.seed(0)
SUBJ = ["the cat", "the dog", "a bird"]
VERB = ["sat on", "saw", "chased"]
OBJ = ["the mat", "the log", "the cat", "a dog"]
# A toy "internet": 400 random sentences from a tiny grammar, so the next
# character is genuinely uncertain at some positions (e.g. after "sat on the ").
TEXT = " ".join(f"{random.choice(SUBJ)} {random.choice(VERB)} {random.choice(OBJ)}."
                for _ in range(400)) + " "

chars = sorted(set(TEXT))
stoi = {c: i for i, c in enumerate(chars)}
itos = {i: c for c, i in stoi.items()}
encode = lambda s: [stoi[c] for c in s]
decode = lambda ids: "".join(itos[i] for i in ids)
data = torch.tensor(encode(TEXT))

V, T, D, H, L = len(chars), 32, 64, 4, 2   # vocab, context, width, heads, layers


class Block(nn.Module):
    def __init__(self):
        super().__init__()
        self.ln1, self.ln2 = nn.LayerNorm(D), nn.LayerNorm(D)
        self.attn = nn.MultiheadAttention(D, H, batch_first=True)
        self.ffn = nn.Sequential(nn.Linear(D, 4 * D), nn.GELU(), nn.Linear(4 * D, D))

    def forward(self, x):
        t = x.size(1)
        mask = torch.triu(torch.ones(t, t, dtype=torch.bool), diagonal=1)  # True = blocked
        h = self.ln1(x)
        x = x + self.attn(h, h, h, attn_mask=mask, need_weights=False)[0]
        return x + self.ffn(self.ln2(x))


class TinyGPT(nn.Module):
    def __init__(self):
        super().__init__()
        self.tok = nn.Embedding(V, D)
        self.pos = nn.Embedding(T, D)           # learned absolute positions (GPT-2 style)
        self.blocks = nn.Sequential(*[Block() for _ in range(L)])
        self.ln = nn.LayerNorm(D)
        self.head = nn.Linear(D, V)

    def forward(self, idx):
        x = self.tok(idx) + self.pos(torch.arange(idx.size(1)))
        return self.head(self.ln(self.blocks(x)))   # logits: (B, t, V)


def batch(bs=32):
    ix = torch.randint(len(data) - T - 1, (bs,))
    x = torch.stack([data[i:i + T] for i in ix])
    y = torch.stack([data[i + 1:i + T + 1] for i in ix])   # targets = inputs shifted by one
    return x, y


@torch.no_grad()
def generate(model, prompt, n, temperature=1.0):
    idx = torch.tensor([encode(prompt)])
    for _ in range(n):
        logits = model(idx[:, -T:])[:, -1, :]               # only the last position matters
        if temperature == 0:
            nxt = logits.argmax(-1, keepdim=True)            # greedy
        else:
            probs = F.softmax(logits / temperature, dim=-1)
            nxt = torch.multinomial(probs, 1)                # sample
        idx = torch.cat([idx, nxt], dim=1)                   # append and repeat
    return decode(idx[0].tolist())


if __name__ == "__main__":
    model = TinyGPT()
    n_params = sum(p.numel() for p in model.parameters())
    print(f"vocab={V} chars, params={n_params:,}")
    print(f"loss of a uniform guess = ln({V}) = {math.log(V):.3f}")
    opt = torch.optim.AdamW(model.parameters(), lr=3e-3)
    start = time.time()
    for step in range(401):
        x, y = batch()
        logits = model(x)
        loss = F.cross_entropy(logits.reshape(-1, V), y.reshape(-1))
        opt.zero_grad()
        loss.backward()
        opt.step()
        if step % 100 == 0:
            print(f"step {step:3d}  loss {loss.item():.3f}  perplexity {math.exp(loss.item()):5.2f}")
    print(f"trained in {time.time() - start:.1f}s")

    model.eval()
    ctx = torch.tensor([encode("the cat sat on the ")])
    probs = F.softmax(model(ctx)[0, -1], dim=-1)
    top = torch.topk(probs, 3)
    print("P(next char | 'the cat sat on the '):",
          {itos[i]: round(p, 3) for p, i in zip(top.values.tolist(), top.indices.tolist())})
    print("greedy     :", repr(generate(model, "the dog ", 40, temperature=0)))
    torch.manual_seed(1)
    print("sample T=1 :", repr(generate(model, "the dog ", 40, temperature=1.0)))
