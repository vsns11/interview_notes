"""CLIP in miniature: contrastive learning of a shared image-text space.

Each "concept" (e.g. 'red square') has a hidden meaning vector. The toy
"image" and "caption" are two DIFFERENT noisy views of that meaning, with
different sizes (48-d vs 32-d), so they cannot be compared directly.
We train two small encoders so that matching (image, caption) pairs get a
high cosine similarity and non-matching pairs a low one, with CLIP's
symmetric cross-entropy loss over an N x N similarity matrix.

Afterwards: zero-shot classification (pick the caption most similar to an
image) and a CLIP-score-style number for each pair.

Run:  python code/clip_contrastive_toy.py   (~5 s)
"""
import numpy as np
import torch
import torch.nn as nn
import torch.nn.functional as F

torch.manual_seed(0)
torch.set_num_threads(1)
colours = ["red", "green", "blue", "yellow"]
shapes = ["square", "circle", "star"]
CONCEPTS = [f"{c} {s}" for c in colours for s in shapes]          # 12 concepts
K, D_MEAN, D_IMG, D_TXT = len(CONCEPTS), 16, 48, 32
meaning = torch.randn(K, D_MEAN)
to_img, to_txt = torch.randn(D_MEAN, D_IMG), torch.randn(D_MEAN, D_TXT)


def make_pairs(n):
    k = torch.randint(0, K, (n,))
    m = meaning[k] + 0.8 * torch.randn(n, D_MEAN)          # the same scene...
    img = m @ to_img + 3.0 * torch.randn(n, D_IMG)         # ...seen as pixels
    txt = meaning[k] @ to_txt + 0.5 * torch.randn(n, D_TXT)  # ...and described
    return img, txt, k


img_enc = nn.Sequential(nn.Linear(D_IMG, 64), nn.ReLU(), nn.Linear(64, 32))
txt_enc = nn.Sequential(nn.Linear(D_TXT, 64), nn.ReLU(), nn.Linear(64, 32))
log_temp = nn.Parameter(torch.tensor(np.log(1 / 0.07), dtype=torch.float32))  # CLIP's init
opt = torch.optim.Adam([*img_enc.parameters(), *txt_enc.parameters(), log_temp], lr=1e-3)


def embed(img, txt):
    return F.normalize(img_enc(img), dim=1), F.normalize(txt_enc(txt), dim=1)


if __name__ == "__main__":
    for step in range(1, 1501):
        img, txt, k = make_pairs(64)
        zi, zt = embed(img, txt)
        logits = log_temp.exp() * zi @ zt.T               # 64 x 64 similarities
        target = torch.arange(64)                          # pair i matches caption i
        # (toy caveat: two pairs of the same concept count as negatives, as in CLIP)
        loss = (F.cross_entropy(logits, target) + F.cross_entropy(logits.T, target)) / 2
        opt.zero_grad(); loss.backward(); opt.step()
        if step in (1, 500, 1500):
            print(f"step {step:4d}  contrastive loss {loss.item():.3f}")

    with torch.no_grad():
        # zero-shot: one clean caption embedding per concept (the "prompt")
        prompts = meaning @ to_txt
        _, zt = embed(torch.zeros(K, D_IMG), prompts)
        img, _, k = make_pairs(1000)
        zi, _ = embed(img, torch.zeros(1000, D_TXT))
        pred = (zi @ zt.T).argmax(1)
        print(f"\nzero-shot accuracy on 1000 new images: {(pred == k).float().mean():.1%} "
              f"(chance = {1 / K:.1%})")
        cos = (zi * zt[k]).sum(1)
        wrong = (zi * zt[(k + 1) % K]).sum(1)
        print(f"CLIP-score style: mean cos(image, its caption) = {cos.mean():.2f}, "
              f"with a wrong caption = {wrong.mean():.2f}")
        print(f"example: image of '{CONCEPTS[k[0]]}' -> predicted '{CONCEPTS[pred[0]]}'")
