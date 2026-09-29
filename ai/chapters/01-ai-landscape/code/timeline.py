"""Plot a timeline of AI milestones, with the two 'AI winters' shaded."""
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from pathlib import Path

MILESTONES = [  # (year, label, label height: + above the axis, - below)
    (1950, "Turing: 'Computing Machinery\nand Intelligence'", 1.2),
    (1956, "Dartmouth workshop\n('artificial intelligence')", 2.3),
    (1958, "Rosenblatt perceptron", -1.3),
    (1966, "ELIZA chatbot", 1.2),
    (1980, "Expert systems boom\n(e.g. XCON at DEC)", 2.3),
    (1986, "Backpropagation popularised", -1.3),
    (1997, "Deep Blue beats Kasparov", 1.2),
    (2012, "AlexNet wins ImageNet", -1.3),
    (2016, "AlphaGo beats\nLee Sedol", 1.0),
    (2017, "Transformer paper", 2.6),
    (2020, "GPT-3 /\nAlphaFold 2", -0.9),
    (2022, "ChatGPT\nreleased", 1.7),
    (2024, "Tool-using agents,\nreasoning models", -2.0),
]
ALIGN = {2016: "right", 2017: "left"}  # nudge crowded labels apart
WINTERS = [(1974, 1980, "1st AI winter"), (1987, 1993, "2nd AI winter")]


def main() -> None:
    fig, ax = plt.subplots(figsize=(13, 4.8))
    ax.axhline(0, color="#444", lw=2)
    for start, end, label in WINTERS:
        ax.axvspan(start, end, color="#9ecae1", alpha=0.45)
        ax.text((start + end) / 2, -2.6, label, ha="center", fontsize=9, color="#08519c")
    for year, text, h in MILESTONES:
        ax.plot([year, year], [0, h], color="#6a51a3", lw=1)
        ax.plot(year, 0, "o", color="#6a51a3")
        ax.text(year, h + (0.15 if h > 0 else -0.15), f"{year}\n{text}", ha=ALIGN.get(year, "center"),
                va="bottom" if h > 0 else "top", fontsize=7.5)
    ax.set_xlim(1945, 2028)
    ax.set_ylim(-3.1, 3.6)
    ax.set_yticks([])
    for side in ("left", "right", "top"):
        ax.spines[side].set_visible(False)
    ax.set_title("Seventy years of AI: booms, winters and breakthroughs")
    out = Path(__file__).resolve().parent.parent / "img" / "ai_timeline.png"
    fig.tight_layout()
    fig.savefig(out, dpi=150)
    print(f"milestones plotted: {len(MILESTONES)}")
    print(f"saved {out.name}")


if __name__ == "__main__":
    main()
