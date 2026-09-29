"""A toy MLOps loop in one file, standard library + numpy + scikit-learn only.

  1. data validation   -> reject a bad batch before it trains a model
  2. experiment tracking -> every run logs params, data hash, metrics
  3. model registry    -> versions with stage: candidate / production / archived
  4. promotion gate    -> beat production overall AND on every segment
  5. shadow            -> candidate scores live traffic, output only logged
  6. canary + rollback -> small traffic share, automatic rollback on a guardrail
  7. skew check        -> compare features logged at serving with the training pipeline's
Real tools doing these jobs: MLflow / Weights & Biases (tracking, registry), TFDV / Great Expectations
(validation), Kubeflow / Airflow / Vertex / SageMaker pipelines (orchestration), KServe / Seldon (rollouts).
"""
import hashlib
import json
import tempfile
from dataclasses import dataclass, field
from pathlib import Path

import numpy as np
from sklearn.ensemble import HistGradientBoostingRegressor
from sklearn.linear_model import Ridge
from sklearn.metrics import mean_absolute_error

rng = np.random.default_rng(0)
SCHEMA = {"distance_km": (0.0, 80.0), "queue_now": (0, 60), "hour": (0, 23), "is_new_city": (0, 1)}


def make_batch(n, new_city_share=0.1):
    d = {"distance_km": rng.gamma(2, 1.5, n), "queue_now": rng.poisson(3, n).astype(float),
         "hour": rng.integers(0, 24, n).astype(float), "is_new_city": (rng.random(n) < new_city_share).astype(float)}
    y = (15 + 4 * d["distance_km"] + 2 * d["queue_now"] * (1 + ((d["hour"] >= 18) & (d["hour"] <= 20)))
         + 6 * d["is_new_city"] * (d["distance_km"] > 3) + rng.normal(0, 3, n))
    return d, y


# ---------- 1. data validation ----------
def validate(batch, max_null=0.01):
    problems = []
    for col, (lo, hi) in SCHEMA.items():
        if col not in batch:
            problems.append(f"missing column {col}")
            continue
        v = batch[col]
        null = np.isnan(v).mean()
        bad = ((v < lo) | (v > hi))[~np.isnan(v)].mean()
        if null > max_null:
            problems.append(f"{col}: {null:.1%} nulls")
        if bad > 0.001:
            problems.append(f"{col}: {bad:.1%} out of range [{lo}, {hi}]")
    return problems


def to_X(batch):
    return np.column_stack([batch[c] for c in SCHEMA])


# ---------- 2. tracking + 3. registry ----------
@dataclass
class Registry:
    root: Path
    versions: list = field(default_factory=list)

    def log_run(self, name, params, data, metrics, model):
        data_hash = hashlib.sha256(to_X(data).tobytes()).hexdigest()[:12]
        v = {"version": len(self.versions) + 1, "name": name, "params": params,
             "data_hash": data_hash, "metrics": metrics, "stage": "candidate"}
        (self.root / f"run_{v['version']}.json").write_text(json.dumps(v))
        v["model"] = model
        self.versions.append(v)
        return v

    def production(self):
        return next((v for v in self.versions if v["stage"] == "production"), None)

    def promote(self, v):
        for old in self.versions:
            if old["stage"] == "production":
                old["stage"] = "archived"
        v["stage"] = "production"


# ---------- 4. promotion gate ----------
def evaluate(model, data, y):
    pred = model.predict(to_X(data))
    seg = data["is_new_city"] == 1
    return {"mae": mean_absolute_error(y, pred),
            "mae_mature": mean_absolute_error(y[~seg], pred[~seg]),
            "mae_new_city": mean_absolute_error(y[seg], pred[seg])}


def gate(cand, prod, tolerance=0.01):
    if prod is None:
        return True, "no production model yet"
    c, p = cand["metrics"], prod["metrics"]
    if c["mae"] >= p["mae"]:
        return False, f"overall MAE {c['mae']:.2f} not better than {p['mae']:.2f}"
    for k in ("mae_mature", "mae_new_city"):
        if c[k] > p[k] * (1 + tolerance):
            return False, f"segment {k} regressed {p[k]:.2f} -> {c[k]:.2f}"
    return True, f"MAE {p['mae']:.2f} -> {c['mae']:.2f}, no segment regressed"


def canary(cand_model, prod_model, live, ylive, cand_features=None):
    """Ramp traffic share; roll back if the canary's MAE (labels arrived later) is >5% worse than prod."""
    cand_live = cand_features or live
    for share in (0.01, 0.05, 0.25, 0.50):
        idx = rng.random(len(ylive)) < share
        mae_c = mean_absolute_error(ylive[idx], cand_model.predict(to_X(cand_live))[idx])
        mae_p = mean_absolute_error(ylive[~idx], prod_model.predict(to_X(live))[~idx])
        if mae_c > mae_p * 1.05:
            print(f"   canary {share:4.0%}: MAE {mae_c:.2f} vs prod {mae_p:.2f} -> ROLLBACK (guardrail: >5% worse)")
            return False
        print(f"   canary {share:4.0%}: MAE {mae_c:.2f} vs prod {mae_p:.2f} -> continue ramp")
    return True


if __name__ == "__main__":
    root = Path(tempfile.mkdtemp())
    reg = Registry(root)
    train, ytr = make_batch(30_000)
    holdout, yho = make_batch(10_000)
    live, ylive = make_batch(20_000)

    print("1) validation of a clean batch:", validate(train) or "OK")
    bad = dict(train); bad["distance_km"] = train["distance_km"] * 1000   # someone switched km -> m
    bad["hour"] = train["hour"].copy(); bad["hour"][:600] = np.nan
    print("   validation of a broken batch:", validate(bad))

    print("\n2-3) train + track + register; v1 (ridge) bootstraps production")
    no_new = train["is_new_city"] == 0                       # a candidate trained without new-city rows
    runs = [("ridge", Ridge(alpha=1.0), {"alpha": 1.0}, train, ytr),
            ("gbdt", HistGradientBoostingRegressor(random_state=0), {"max_iter": 100}, train, ytr),
            ("gbdt_no_new_city", HistGradientBoostingRegressor(random_state=0), {"max_iter": 100, "rows": "mature only"},
             {k: v[no_new] for k, v in train.items()}, ytr[no_new])]
    for name, model, params, data, y in runs:
        model.fit(to_X(data), y)
        v = reg.log_run(name, params, data, evaluate(model, holdout, yho), model)
        m = v["metrics"]
        print(f"   v{v['version']} {name:16s} MAE {m['mae']:.2f} (mature {m['mae_mature']:.2f}, new city {m['mae_new_city']:.2f})")
    reg.promote(reg.versions[0])
    print("   tracked run files:", sorted(p.name for p in root.iterdir()))

    print("\n4) promotion gate vs production (v1)")
    for v in reg.versions[1:]:
        ok, why = gate(v, reg.production())
        v["gate"] = ok
        print(f"   v{v['version']} {v['name']:16s} -> {'PASS' if ok else 'FAIL'}: {why}")

    cand, prod = reg.versions[1], reg.production()
    p_prod, p_cand = prod["model"].predict(to_X(live)), cand["model"].predict(to_X(live))
    print(f"\n5) shadow v{cand['version']}: {len(ylive):,} live requests, mean |prod - cand| = "
          f"{np.abs(p_prod - p_cand).mean():.2f} min, max = {np.abs(p_prod - p_cand).max():.1f} min (logged, never served)")

    # 7) the new serving container re-implemented 'hour' in UTC instead of local time (UTC-5): skew
    skewed = dict(live); skewed["hour"] = (live["hour"] + 5) % 24
    print("\n6) canary v2, first attempt (serving container has the UTC-hour bug):")
    canary(cand["model"], prod["model"], live, ylive, cand_features=skewed)
    print(f"7) skew check on logged features: 'hour' differs from the training pipeline on "
          f"{(skewed['hour'] != live['hour']).mean():.0%} of rows -> fix the serving code path")
    print("   canary v2, after the fix:")
    if canary(cand["model"], prod["model"], live, ylive):
        reg.promote(cand)
    print("   registry:", [(v["version"], v["name"], v["stage"]) for v in reg.versions])
