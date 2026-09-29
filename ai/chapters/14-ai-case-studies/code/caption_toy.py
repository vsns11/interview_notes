"""Image captioning: rerank beam candidates for grounding, then score them.

The "image encoder" is faked as an object detector's output (object -> confidence),
and the "decoder" as a list of beam-search candidates with log-probabilities.
We show three things a captioning service needs beyond the model:
  1) a grounding reranker that penalises objects the detector did not see,
  2) CHAIR (Rohrbach et al., 2018) - the share of mentioned objects that are hallucinated,
  3) a consensus metric in the spirit of CIDEr (Vedantam et al., 2015): TF-IDF n-gram
     cosine against several human references,
plus an alt-text policy that does not guess people's gender.
All images, captions and scores are invented for the demo.
"""
from __future__ import annotations

import math
from collections import Counter

OBJECTS = {"dog", "frisbee", "ball", "park", "grass", "man", "woman", "person", "bicycle", "street",
           "car", "cat", "sofa", "laptop", "table", "cup", "pizza", "plate", "bench", "tree"}

IMAGES = [
    dict(id="img1", detected={"dog": .97, "frisbee": .91, "grass": .88, "tree": .40},
         refs=["a dog jumps to catch a frisbee on the grass",
               "a brown dog catching a frisbee in a park",
               "dog leaping for a frisbee outdoors"],
         beams=[("a dog catching a frisbee in the park", -4.1),
                ("a dog playing with a ball on the grass", -3.9),
                ("a dog jumping for a frisbee on the grass", -4.4)]),
    dict(id="img2", detected={"cat": .95, "sofa": .90, "laptop": .35},
         refs=["a cat sleeping on a sofa", "a grey cat curled up on the couch",
               "a cat napping on a sofa cushion"],
         beams=[("a cat sitting on a sofa next to a laptop", -3.2),
                ("a cat sleeping on a sofa", -3.5),
                ("a cat lying on a couch", -3.7)]),
    dict(id="img3", detected={"person": .96, "bicycle": .94, "street": .85, "car": .62},
         refs=["a person rides a bicycle down a city street", "a cyclist on a street with cars",
               "someone riding a bike past parked cars"],
         beams=[("a man riding a bicycle down a street", -2.9),
                ("a person riding a bicycle down a street next to a car", -3.8),
                ("a man riding a bike with a dog", -3.6)]),
    dict(id="img4", detected={"pizza": .98, "plate": .90, "table": .86, "cup": .30},
         refs=["a pizza on a plate on a wooden table", "a whole pizza served on a plate",
               "pizza on a white plate on a table"],
         beams=[("a pizza on a plate with a cup of coffee", -3.0),
                ("a pizza on a plate on a table", -3.3),
                ("a slice of pizza on a table", -3.4)]),
]

SYNONYM = {"couch": "sofa", "bike": "bicycle", "cyclist": "person", "someone": "person"}
GENDERED = {"man": "person", "woman": "person", "men": "people", "women": "people"}


def mentioned(caption: str) -> list[str]:
    out = []
    for w in caption.split():
        w = SYNONYM.get(w, w)
        if w in OBJECTS:
            out.append(w)
    return out


def supported(obj: str, detected: dict, thr: float = 0.5) -> bool:
    if obj in ("man", "woman"):
        obj = "person"
    return detected.get(obj, 0.0) >= thr


def grounding_penalty(caption: str, detected: dict) -> int:
    return sum(not supported(o, detected) for o in mentioned(caption))


def rerank(beams, detected, alpha=2.0):
    return max(beams, key=lambda b: b[1] - alpha * grounding_penalty(b[0], detected))


def chair(captions_and_dets) -> tuple[float, float]:
    objs = hall = caps_with = 0
    for cap, det in captions_and_dets:
        m = mentioned(cap)
        h = sum(not supported(o, det) for o in m)
        objs, hall, caps_with = objs + len(m), hall + h, caps_with + (h > 0)
    return hall / max(objs, 1), caps_with / len(captions_and_dets)


def ngram_vec(text: str, n: int) -> Counter:
    t = text.split()
    return Counter(tuple(t[i:i + n]) for i in range(len(t) - n + 1))


def make_idf(all_refs: list[list[str]], n: int) -> dict:
    df = Counter()
    for refs in all_refs:
        seen = set()
        for r in refs:
            seen |= set(ngram_vec(r, n))
        df.update(seen)
    N = len(all_refs)
    return {g: math.log(N / c) for g, c in df.items()}


def consensus(caption: str, refs: list[str], idfs: list[dict]) -> float:
    """CIDEr-like: mean over n=1..4 of mean TF-IDF cosine with each reference (x10)."""
    score = 0.0
    for n, idf in enumerate(idfs, start=1):
        def vec(s):
            v = ngram_vec(s, n)
            return {g: c * idf.get(g, math.log(len(IMAGES))) for g, c in v.items()}
        cv = vec(caption)
        sims = []
        for r in refs:
            rv = vec(r)
            dot = sum(cv[g] * rv.get(g, 0) for g in cv)
            na = math.sqrt(sum(x * x for x in cv.values())) or 1
            nb = math.sqrt(sum(x * x for x in rv.values())) or 1
            sims.append(dot / (na * nb))
        score += sum(sims) / len(sims)
    return 10 * score / len(idfs)


def alt_text(caption: str) -> str:
    return " ".join(GENDERED.get(w, w) for w in caption.split())


def main() -> None:
    idfs = [make_idf([im["refs"] for im in IMAGES], n) for n in range(1, 5)]
    top, reranked = [], []
    print("image | pick by model log-prob only            | pick after grounding rerank")
    for im in IMAGES:
        a = max(im["beams"], key=lambda b: b[1])[0]
        b = rerank(im["beams"], im["detected"])[0]
        top.append((a, im["detected"]))
        reranked.append((b, im["detected"]))
        print(f"{im['id']:5} | {a:39} | {b}")
    for label, caps in (("log-prob only", top), ("grounding rerank", reranked)):
        ci, cs = chair(caps)
        cons = sum(consensus(c, im["refs"], idfs) for (c, _), im in zip(caps, IMAGES)) / len(IMAGES)
        print(f"{label:17} CHAIR_i {ci:.2f} (hallucinated objects / objects)  "
              f"CHAIR_s {cs:.2f} (captions with one)  consensus {cons:.2f}")
    print("\nalt-text policy (do not infer gender from appearance):")
    c = reranked[2][0]
    print(f"  model: {c!r}\n  shown: {alt_text(c)!r}")


if __name__ == "__main__":
    main()
