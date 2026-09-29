"""Classification metrics: confusion matrix, accuracy/precision/recall/F1, ROC-AUC vs PR-AUC under imbalance, thresholds by cost."""
import numpy as np
from sklearn.metrics import (accuracy_score, average_precision_score, confusion_matrix, f1_score,
                             precision_recall_curve, precision_score, recall_score, roc_auc_score, roc_curve)

from plotstyle import C, plt, save


def main() -> None:
    # 1) Tiny example
    y_true, y_pred = [0, 1, 0, 1], [0, 1, 1, 1]
    print("tiny: acc %.2f  precision %.3f  recall %.2f  F1 %.2f" % (
        accuracy_score(y_true, y_pred), precision_score(y_true, y_pred),
        recall_score(y_true, y_pred), f1_score(y_true, y_pred)))

    # 2) Worked example: 1,000 transactions, 100 fraud; the model flags 80, of which 60 are fraud
    y = np.r_[np.ones(100), np.zeros(900)].astype(int)
    pred = np.r_[np.ones(60), np.zeros(40), np.ones(20), np.zeros(880)].astype(int)
    tn, fp, fn, tp = confusion_matrix(y, pred).ravel()
    print(f"\nconfusion matrix [[TN FP] [FN TP]] =\n{confusion_matrix(y, pred)}")
    print(f"TP={tp} FP={fp} FN={fn} TN={tn}")
    print(f"accuracy  (TP+TN)/N      = {accuracy_score(y, pred):.3f}")
    print(f"precision TP/(TP+FP)     = {precision_score(y, pred):.3f}")
    print(f"recall    TP/(TP+FN)     = {recall_score(y, pred):.3f}")
    print(f"F1        2PR/(P+R)      = {f1_score(y, pred):.3f}")
    print(f"FPR       FP/(FP+TN)     = {fp / (fp + tn):.3f}   specificity = {tn / (tn + fp):.3f}")
    print(f"always-'legit' baseline accuracy = {accuracy_score(y, np.zeros_like(y)):.3f}")

    # 3) ROC-AUC by hand: fraction of (positive, negative) pairs ranked correctly
    s_pos, s_neg = np.array([0.9, 0.7, 0.4]), np.array([0.8, 0.3, 0.2])
    pairs = [(p > n) + 0.5 * (p == n) for p in s_pos for n in s_neg]
    print(f"\nROC-AUC by pair counting: {sum(pairs):.0f}/9 = {np.mean(pairs):.3f}  sklearn "
          f"{roc_auc_score([1, 1, 1, 0, 0, 0], np.r_[s_pos, s_neg]):.3f}")

    # 4) Same score model, two base rates: ROC-AUC unchanged, PR-AUC collapses
    rng = np.random.default_rng(0)
    pos = rng.normal(1.5, 1, 1000)
    fig, axes = plt.subplots(1, 2, figsize=(11, 4))
    for n_neg, col in [(1000, C[0]), (99_000, C[1])]:
        neg = rng.normal(0, 1, n_neg)
        yy = np.r_[np.ones(1000), np.zeros(n_neg)]
        ss = np.r_[pos, neg]
        roc, ap = roc_auc_score(yy, ss), average_precision_score(yy, ss)
        rate = 1000 / (1000 + n_neg)
        print(f"positives {rate:6.1%}: ROC-AUC {roc:.3f} | PR-AUC (avg precision) {ap:.3f} | no-skill PR-AUC {rate:.3f}")
        fpr, tpr, _ = roc_curve(yy, ss)
        prec, rec, _ = precision_recall_curve(yy, ss)
        axes[0].plot(fpr, tpr, color=col, label=f"{rate:.0%} positive (AUC {roc:.2f})")
        axes[1].plot(rec, prec, color=col, label=f"{rate:.0%} positive (AP {ap:.2f})")
    axes[0].plot([0, 1], [0, 1], "--", color="#8a8983", linewidth=1, label="random")
    axes[0].set(xlabel="false positive rate", ylabel="true positive rate (recall)", title="ROC: identical for both base rates")
    axes[1].set(xlabel="recall", ylabel="precision", title="Precision-recall: rare positives expose false alarms")
    for ax in axes:
        ax.legend(loc="lower right" if ax is axes[0] else "upper right")
    save(fig, "roc_vs_pr.png")

    # 5) Picking a threshold by expected cost: a missed fraud costs $100, a false alarm $5 (review time)
    neg = rng.normal(0, 1, 19_000)
    yy = np.r_[np.ones(1000), np.zeros(19_000)]
    # True posterior for two unit-variance Gaussians (means 1.5 and 0) at a 5% base rate -> calibrated p
    p = 1 / (1 + np.exp(-(1.5 * np.r_[pos, neg] - 1.125 + np.log(1000 / 19_000))))
    print("\nthreshold  precision  recall  flagged   cost($)")
    best = None
    for t in [0.01, 0.025, 0.048, 0.1, 0.2, 0.5]:
        f = p >= t
        tp_, fp_ = (f & (yy == 1)).sum(), (f & (yy == 0)).sum()
        fn_ = ((~f) & (yy == 1)).sum()
        cost = 100 * fn_ + 5 * fp_
        best = min(best or (cost, t), (cost, t))
        print(f"{t:>9} {tp_ / max(tp_ + fp_, 1):>10.3f} {tp_ / 1000:>7.3f} {f.sum():>8} {cost:>9,}")
    print(f"cheapest threshold: {best[1]}  (theory for calibrated p: flag if p > 5/(5+100) = {5 / 105:.3f})")


if __name__ == "__main__":
    main()
