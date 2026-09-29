"""A decoder-only transformer block in PyTorch, written from scratch.

Checks: (1) our attention == torch's fused scaled_dot_product_attention,
(2) causal masking really hides the future, (3) parameter counts.
"""
import torch
import torch.nn as nn
import torch.nn.functional as F

torch.manual_seed(0)


class MultiHeadSelfAttention(nn.Module):
    def __init__(self, d_model: int, n_heads: int):
        super().__init__()
        assert d_model % n_heads == 0
        self.h, self.d_head = n_heads, d_model // n_heads
        self.qkv = nn.Linear(d_model, 3 * d_model)   # W_Q, W_K, W_V in one matrix
        self.out = nn.Linear(d_model, d_model)       # W_O mixes the heads

    def forward(self, x: torch.Tensor, causal: bool = True) -> torch.Tensor:
        B, T, C = x.shape
        q, k, v = self.qkv(x).split(C, dim=-1)
        # (B, T, C) -> (B, h, T, d_head): each head works on its own slice
        q, k, v = (t.view(B, T, self.h, self.d_head).transpose(1, 2) for t in (q, k, v))
        scores = q @ k.transpose(-2, -1) / self.d_head ** 0.5       # (B, h, T, T)
        if causal:
            mask = torch.tril(torch.ones(T, T, dtype=torch.bool, device=x.device))
            scores = scores.masked_fill(~mask, float("-inf"))
        weights = scores.softmax(dim=-1)
        y = weights @ v                                              # (B, h, T, d_head)
        y = y.transpose(1, 2).contiguous().view(B, T, C)             # concat heads
        return self.out(y)


class TransformerBlock(nn.Module):
    """Pre-LayerNorm block, as in GPT-2 and most modern LLMs."""

    def __init__(self, d_model: int, n_heads: int, d_ff: int):
        super().__init__()
        self.ln1 = nn.LayerNorm(d_model)
        self.attn = MultiHeadSelfAttention(d_model, n_heads)
        self.ln2 = nn.LayerNorm(d_model)
        self.ffn = nn.Sequential(nn.Linear(d_model, d_ff), nn.GELU(), nn.Linear(d_ff, d_model))

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        x = x + self.attn(self.ln1(x))   # residual connection around attention
        x = x + self.ffn(self.ln2(x))    # residual connection around the FFN
        return x


if __name__ == "__main__":
    B, T, d_model, n_heads = 2, 5, 16, 4
    x = torch.randn(B, T, d_model)

    # (1) Compare our attention math with PyTorch's fused kernel
    mha = MultiHeadSelfAttention(d_model, n_heads)
    ours = mha(x)
    q, k, v = mha.qkv(x).split(d_model, dim=-1)
    q, k, v = (t.view(B, T, n_heads, d_model // n_heads).transpose(1, 2) for t in (q, k, v))
    ref = F.scaled_dot_product_attention(q, k, v, is_causal=True)
    ref = mha.out(ref.transpose(1, 2).reshape(B, T, d_model))
    print("max |ours - torch SDPA| =", f"{(ours - ref).abs().max().item():.2e}")

    # (2) Causality: changing the LAST token must not change outputs at earlier positions
    block = TransformerBlock(d_model, n_heads, d_ff=4 * d_model)
    y1 = block(x)
    x2 = x.clone()
    x2[:, -1] += 10.0
    y2 = block(x2)
    diff = (y1 - y2).abs().amax(dim=(0, 2))
    print("per-position change after editing the last token:", [round(d, 3) for d in diff.tolist()])

    # (3) Shapes and parameter counts
    print("input shape:", tuple(x.shape), "-> output shape:", tuple(y1.shape))
    n_attn = sum(p.numel() for p in block.attn.parameters())
    n_ffn = sum(p.numel() for p in block.ffn.parameters())
    n_ln = sum(p.numel() for p in block.ln1.parameters()) + sum(p.numel() for p in block.ln2.parameters())
    print(f"params: attention={n_attn}, ffn={n_ffn}, layernorms={n_ln}, total={n_attn + n_ffn + n_ln}")
    print("rule of thumb 12*d_model^2 =", 12 * d_model ** 2)
