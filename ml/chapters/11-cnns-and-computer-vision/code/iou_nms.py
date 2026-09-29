"""Object-detection building blocks: Intersection-over-Union (IoU) and Non-Maximum Suppression (NMS).
(torchvision.ops.box_iou / torchvision.ops.nms do the same; torchvision isn't needed here.)"""
import numpy as np


def iou(a, b):
    """Boxes as (x1, y1, x2, y2)."""
    ix1, iy1 = max(a[0], b[0]), max(a[1], b[1])
    ix2, iy2 = min(a[2], b[2]), min(a[3], b[3])
    inter = max(0, ix2 - ix1) * max(0, iy2 - iy1)
    area = lambda r: (r[2] - r[0]) * (r[3] - r[1])
    return inter / (area(a) + area(b) - inter)


def my_nms(boxes, scores, thr):
    order = sorted(range(len(boxes)), key=lambda i: -scores[i])
    keep = []
    while order:
        best = order.pop(0)
        keep.append(best)
        order = [i for i in order if iou(boxes[best], boxes[i]) < thr]   # drop heavy overlaps
    return keep


if __name__ == "__main__":
    truth = (50, 50, 150, 150)
    pred = (70, 60, 170, 160)
    print(f"IoU worked example: {iou(truth, pred):.3f}")
    boxes = [(50, 50, 150, 150), (55, 52, 152, 148), (60, 60, 160, 165), (300, 80, 380, 160), (305, 85, 385, 170)]
    scores = [0.90, 0.85, 0.60, 0.80, 0.75]
    keep = my_nms(boxes, scores, thr=0.5)
    print("pairwise IoU of the 5 boxes:")
    print(np.array([[iou(a, b) for b in boxes] for a in boxes]).round(2))
    print("NMS (IoU threshold 0.5) keeps boxes", keep, "with scores", [scores[i] for i in keep])
