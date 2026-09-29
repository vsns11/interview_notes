"""Tiny encoder-decoder (seq2seq) with GRUs: reverse a sequence of digits.
Compares a plain seq2seq (decoder sees only the encoder's final state) with one that uses
dot-product (Luong-style) attention over all encoder states."""
import torch
from torch import nn

torch.set_num_threads(1)
VOCAB, SOS, L, H = 10, 10, 12, 64          # digits 0-9, start-of-sequence token id 10, length 12


def batch(n, gen):
    src = torch.randint(0, VOCAB, (n, L), generator=gen)
    return src, src.flip(1)                 # target = reversed source


class Seq2Seq(nn.Module):
    def __init__(self, use_attention):
        super().__init__()
        self.use_attention = use_attention
        self.src_emb = nn.Embedding(VOCAB, 32)
        self.tgt_emb = nn.Embedding(VOCAB + 1, 32)                  # +1 for <sos>
        self.encoder = nn.GRU(32, H, batch_first=True)
        self.decoder = nn.GRU(32, H, batch_first=True)
        self.out = nn.Linear(2 * H if use_attention else H, VOCAB)

    def forward(self, src, tgt_in):
        enc_out, h = self.encoder(self.src_emb(src))                # enc_out: (B, L, H) - one vector per input token
        dec_out, _ = self.decoder(self.tgt_emb(tgt_in), h)          # decoder starts from the encoder's summary h
        if not self.use_attention:
            return self.out(dec_out), None
        scores = dec_out @ enc_out.transpose(1, 2) / H ** 0.5       # (B, L_tgt, L_src): query=decoder, keys=encoder
        weights = scores.softmax(-1)
        context = weights @ enc_out                                 # weighted sum of encoder states
        return self.out(torch.cat([dec_out, context], -1)), weights

    @torch.no_grad()
    def greedy(self, src):
        tok = torch.full((len(src), 1), SOS)
        for _ in range(L):                                          # feed back our own predictions
            logits, w = self(src, tok)
            tok = torch.cat([tok, logits[:, -1:].argmax(-1)], 1)
        return tok[:, 1:], w


def train(use_attention, steps=1500):
    gen = torch.Generator().manual_seed(0); torch.manual_seed(0)
    model = Seq2Seq(use_attention)
    opt = torch.optim.Adam(model.parameters(), lr=3e-3)
    for _ in range(steps):
        src, tgt = batch(64, gen)
        tgt_in = torch.cat([torch.full((64, 1), SOS), tgt[:, :-1]], 1)   # teacher forcing: shift right
        logits, _ = model(src, tgt_in)
        loss = nn.functional.cross_entropy(logits.reshape(-1, VOCAB), tgt.reshape(-1))
        opt.zero_grad(); loss.backward(); nn.utils.clip_grad_norm_(model.parameters(), 1.0); opt.step()
    src, tgt = batch(500, gen)
    pred, w = model.greedy(src)
    tok_acc = (pred == tgt).float().mean().item()
    seq_acc = (pred == tgt).all(1).float().mean().item()
    return tok_acc, seq_acc, src[0], pred[0], w


if __name__ == "__main__":
    for att in [False, True]:
        tok, seq, s, p, w = train(att)
        print(f"{'with attention' if att else 'no attention  '}: token accuracy {tok:.3f}, whole-sequence accuracy {seq:.3f}")
        print(f"   source {s.tolist()}\n   output {p.tolist()}")
        if att:
            print("   where each output step looks (argmax of attention over source positions):",
                  w[0].argmax(-1).tolist())
