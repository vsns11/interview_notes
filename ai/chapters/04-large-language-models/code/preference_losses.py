"""The two preference-learning losses behind RLHF and DPO, on toy numbers.

reward-model loss (Bradley-Terry): -log sigmoid(r_chosen - r_rejected)
DPO loss: -log sigmoid(beta * [(logpi_c - logref_c) - (logpi_r - logref_r)])
"""
import math


def sigmoid(x: float) -> float:
    return 1 / (1 + math.exp(-x))


def reward_model_loss(r_chosen: float, r_rejected: float) -> float:
    return -math.log(sigmoid(r_chosen - r_rejected))


def dpo_loss(logp_c, logp_r, ref_c, ref_r, beta=0.1):
    margin = (logp_c - ref_c) - (logp_r - ref_r)   # how much MORE the policy prefers chosen than the reference does
    return -math.log(sigmoid(beta * margin)), margin


if __name__ == "__main__":
    print("Reward model: a human preferred answer A over answer B")
    for rc, rr in [(0.0, 0.0), (2.0, 0.0), (0.0, 2.0), (5.0, 0.0)]:
        print(f"  r(A)={rc:+.1f} r(B)={rr:+.1f} -> P(A preferred)={sigmoid(rc - rr):.3f}, "
              f"loss={reward_model_loss(rc, rr):.3f}")

    print("\nDPO (log-probabilities of whole responses; reference = frozen SFT model)")
    ref_c, ref_r = -40.0, -38.0   # the SFT model slightly prefers the REJECTED answer
    for label, lc, lr in [("policy = reference     ", -40.0, -38.0),
                          ("policy raises chosen   ", -30.0, -38.0),
                          ("policy lowers rejected ", -40.0, -55.0),
                          ("policy moves wrong way ", -45.0, -33.0)]:
        loss, margin = dpo_loss(lc, lr, ref_c, ref_r)
        print(f"  {label}: margin={margin:+6.1f}, loss={loss:.3f}")
