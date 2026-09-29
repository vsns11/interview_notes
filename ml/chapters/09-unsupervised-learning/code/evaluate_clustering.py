"""Internal vs external clustering metrics, and a 'silhouette lies on non-convex shapes' check."""
from sklearn.cluster import DBSCAN, KMeans
from sklearn.datasets import load_iris, make_moons
from sklearn.metrics import (adjusted_rand_score, calinski_harabasz_score, davies_bouldin_score,
                             normalized_mutual_info_score, silhouette_score)
from sklearn.preprocessing import StandardScaler


def report(name, X, labels, y):
    print(f"{name:22s} silhouette={silhouette_score(X, labels):6.3f}  DB={davies_bouldin_score(X, labels):5.2f}  "
          f"CH={calinski_harabasz_score(X, labels):7.1f}  ARI={adjusted_rand_score(y, labels):5.2f}  "
          f"NMI={normalized_mutual_info_score(y, labels):5.2f}")


if __name__ == "__main__":
    X, y = load_iris(return_X_y=True)
    Xs = StandardScaler().fit_transform(X)
    print("iris (true k = 3 species)")
    for k in (2, 3, 4):
        report(f"  k-means k={k}", Xs, KMeans(n_clusters=k, n_init=10, random_state=0).fit_predict(Xs), y)

    Xm, ym = make_moons(n_samples=400, noise=0.06, random_state=0)
    Xm = StandardScaler().fit_transform(Xm)
    print("\nmoons (true k = 2, non-convex)")
    report("  k-means k=2", Xm, KMeans(n_clusters=2, n_init=10, random_state=0).fit_predict(Xm), ym)
    report("  DBSCAN eps=0.3", Xm, DBSCAN(eps=0.3, min_samples=5).fit_predict(Xm), ym)
