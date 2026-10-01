# Machine Learning Handbook

One place to learn machine learning from "what is a model?" to confident intermediate practice, and to prepare for ML and ML-system-design interviews, without switching between platforms. Each topic combines, in one explanation:
- **the DesignGurus courses** you own: *Grokking Modern AI Fundamentals* (the big picture, data, neural networks, recommenders) and *Grokking the AI System Design Interview* (the design framework, production ML and the case studies),
- **classic books**: *Hands-On Machine Learning with Scikit-Learn, Keras and TensorFlow* (Géron), *An Introduction to Statistical Learning* (James, Witten, Hastie, Tibshirani), *Designing Machine Learning Systems* (Huyen), *Machine Learning System Design Interview* (Aminian and Xu), *Deep Learning* (Goodfellow, Bengio, Courville) and *Reinforcement Learning: An Introduction* (Sutton and Barto),
- **official documentation and papers** (scikit-learn, PyTorch, numpy, pandas, the original papers for key methods) and the web, to fill gaps and check facts,
- **real-world examples** from products you use (search, feeds, ads, fraud checks, recommendations, maps).

Every chapter is written for a first-time learner and defines each term the first time it appears. The math is shown in plain text with worked numbers. Every code example is a runnable script (Python 3.14, compatible with 3.12+, CPU only) whose **real output** appears under it.

> Personal study notes. Course-derived material is paraphrased in my own words, not copied; a few course diagrams are embedded and captioned *(course diagram)*. Not for redistribution.

## Who it's for
| Reader | What you get |
|---|---|
| Complete beginners to ML | The ideas from zero (Ch 1), just enough math (Ch 2) and the Python tools (Ch 3), every term defined |
| Developers and analysts who know some Python | Classic algorithms with scikit-learn (Ch 4–9), then deep learning with PyTorch (Ch 10–12) |
| Engineers moving into ML roles | Recommenders, RL, and how models are built, served and monitored in production (Ch 13–16) |
| Interview candidates | The 7-step ML system design framework (Ch 15), thirteen worked case studies (Ch 17–18), responsible ML (Ch 19) and one-page cheat sheets with 50 interview questions (Ch 20) |

## How to use it
Every topic follows the same rhythm: *what it is → why it matters → how it works → code with real output → trade-offs → when not to use it → common mistakes → real-world examples → in the interview → self-check → TL;DR*. To review, skim the TL;DRs. To check you really learned a topic, answer the self-check questions without looking back.

**Beginner path (read in order):** 1 → 2 → 3 → 4 → 5 → 6 (the core: data, the first models, and how to evaluate them), then 7 → 8 → 9 (more algorithms), then 10 → 11 → 12 (deep learning), then 13 → 14, then 15 → 16 → 17 → 18 → 19. Run each chapter's scripts and change a parameter or two. Keep Chapter 20 open as a quick reference.

**Interview path (you already know some ML):**
1. Skim the TL;DRs of Ch 4–9 to fill gaps (leakage, regularisation, metrics, trees vs linear models, clustering).
2. Read **Ch 6** closely: metric choice, calibration, cross-validation and bias–variance come up in almost every interview.
3. Skim Ch 10–13 for deep learning and recommender basics (backprop, CNNs, attention, embeddings, two-tower retrieval).
4. Study **Ch 15 → 16 → 17 → 18** for ML system design, and **Ch 19** for fairness and privacy questions.
5. The night before, review with **Ch 20** (decision tables, the top 50 questions and the glossary), practising the 30-second answers out loud.

**ML system design only (you know the models, you need the design round):**
1. **Ch 15** — the seven-step framework and how to scope a vague question.
2. **Ch 16** — training vs serving, feature stores, batch vs online inference, offline metrics, drift, A/B tests, MLOps.
3. **Ch 17 and 18** — thirteen case studies (video recommendations, feed ranking, search, video search, ads, fraud, moderation, PYMK, music, visual search, street-imagery blurring, event recommendation, similar listings), each worked through the same template.
4. Refresh as needed: Ch 6 (metrics), Ch 13 (recommenders and ANN search), Ch 19 (responsible ML); finish with Ch 20.13–20.14 and 20.16.

```mermaid
flowchart LR
    A["Part I - Foundations: Ch 1-4"] --> B["Part II - Classic ML: Ch 5-9"]
    B --> C["Part III - Deep learning: Ch 10-12"]
    C --> D["Part IV - Recommenders and RL: Ch 13-14"]
    D --> E["Part V - ML systems: Ch 15-19"]
    E --> F["Part VI - Revision: Ch 20"]
```

## What's in here
| Path | Purpose |
|---|---|
| `ML-Handbook.pdf` | The whole book as one PDF, with a cover and a table of contents (built by `tools/regen_pdfs.sh`). |
| `interview/INTERVIEW.md` + `interview/ML-Interview-Edition.pdf` | **Interview Edition** — a ~48-page fast-revision digest: every chapter's must-know terms, formula tables (losses, metrics, bias–variance, regularisation, gradient descent, attention), top questions, gotchas, the 7-step design framework with napkin math and all thirteen case studies in five lines each. |
| `capstones/CAPSTONES.md` + `capstones/ML-Capstone-Projects.pdf` | **Capstone Projects**: 40 build-it-yourself design briefs, beginner to advanced (house prices, Titanic CV, spam filter, churn + SHAP, transfer learning, recommenders, forecasting, A/B tests, two-tower retrieval, feed and search ranking, streaming fraud, CTR, feature store + drift monitoring, RL, fairness audit, RAG), each with requirements, a pipeline diagram, data model, milestones, hard parts and interview Q&A. |
| `chapters/NN-name/README.md` + `ChapterNN-Notes.pdf` | One chapter (Markdown source and its PDF). |
| `chapters/NN-name/code/` | Runnable scripts for that chapter (each has an `if __name__ == "__main__":` block; `plotstyle.py` files are shared helpers). |
| `chapters/NN-name/img/` | Plots generated by the chapter's scripts, plus a few course diagrams. |
| `tools/` | `regen_pdfs.sh` (rebuild all PDFs) and `md2pdf.mjs` (Markdown → PDF). |

## Chapters
| # | Chapter | In one line | PDF |
|---|---|---|---|
| **Part I — Foundations** | | | |
| 01 | [What Machine Learning Is](chapters/01-what-is-ml/README.md) | AI vs ML vs deep learning, learning setups, classification vs regression, key terms, the end-to-end workflow, the main failure modes, a first scikit-learn project | `Chapter01-Notes.pdf` |
| 02 | [The Math You Need for Machine Learning](chapters/02-math-for-ml/README.md) | Vectors, matrices, SVD, gradients and gradient descent, probability and Bayes, statistics, hypothesis tests, entropy and KL divergence | `Chapter02-Notes.pdf` |
| 03 | [The Python ML Stack](chapters/03-python-ml-stack/README.md) | Setting up, NumPy, pandas, matplotlib, the scikit-learn estimator API, Jupyter, when to reach for PyTorch | `Chapter03-Notes.pdf` |
| 04 | [Data, EDA and Feature Engineering](chapters/04-data-eda-feature-engineering/README.md) | Data sources and labels, EDA, cleaning, encoding, scaling, feature creation, splits, leakage, class imbalance, `Pipeline` + `ColumnTransformer` | `Chapter04-Notes.pdf` |
| **Part II — Classic ML** | | | |
| 05 | [Linear and Logistic Regression](chapters/05-linear-and-logistic-regression/README.md) | Loss, normal equation vs gradient descent, assumptions, polynomial features, Ridge/Lasso/ElasticNet, logistic regression, softmax, reading coefficients | `Chapter05-Notes.pdf` |
| 06 | [Evaluation and Tuning](chapters/06-evaluation-and-tuning/README.md) | Baselines, regression/classification/ranking metrics, thresholds, calibration, cross-validation, bias–variance, learning curves, hyperparameter search, early stopping | `Chapter06-Notes.pdf` |
| 07 | [Decision Trees and Ensembles](chapters/07-trees-and-ensembles/README.md) | Trees and impurity, bagging and random forests, AdaBoost and gradient boosting (XGBoost/LightGBM ideas), voting and stacking, when trees beat neural nets | `Chapter07-Notes.pdf` |
| 08 | [k-NN, SVMs and Naive Bayes](chapters/08-knn-svm-naive-bayes/README.md) | Distance-based k-NN, maximum-margin SVMs, kernels and SVR, naive Bayes for text, and how to choose a classic algorithm | `Chapter08-Notes.pdf` |
| 09 | [Unsupervised Learning](chapters/09-unsupervised-learning/README.md) | k-means, hierarchical clustering, DBSCAN, Gaussian mixtures and EM, PCA, t-SNE/UMAP, anomaly detection, association rules, evaluation without labels | `Chapter09-Notes.pdf` |
| **Part III — Deep learning** | | | |
| 10 | [Neural Networks and Deep Learning](chapters/10-neural-networks-and-deep-learning/README.md) | Perceptron to MLP, activations, losses, backpropagation, optimizers and schedules, initialization, regularization, training habits, PyTorch basics | `Chapter10-Notes.pdf` |
| 11 | [CNNs and Computer Vision](chapters/11-cnns-and-computer-vision/README.md) | Convolution and pooling, a small CNN in PyTorch, famous architectures, transfer learning, augmentation, detection and segmentation | `Chapter11-Notes.pdf` |
| 12 | [Sequences: RNNs, LSTMs and Attention](chapters/12-sequences-rnns-and-attention/README.md) | RNNs and vanishing gradients, LSTM/GRU, seq2seq, attention, the transformer at a high level, tokenization, word embeddings, time-series forecasting | `Chapter12-Notes.pdf` |
| **Part IV — Recommenders and reinforcement learning** | | | |
| 13 | [Recommender Systems and Embeddings](chapters/13-recommenders-and-embeddings/README.md) | Feedback types, similarity, content-based vs collaborative filtering, matrix factorisation, embeddings, two-tower retrieval, the funnel, ANN search, cold start, evaluation | `Chapter13-Notes.pdf` |
| 14 | [Reinforcement Learning](chapters/14-reinforcement-learning/README.md) | MDPs, Bellman equations, bandits, dynamic programming, Monte Carlo vs TD, Q-learning and SARSA, DQN, policy gradients and actor-critic, RLHF | `Chapter14-Notes.pdf` |
| **Part V — ML systems** | | | |
| 15 | [ML System Design: The Interview Framework](chapters/15-ml-system-design-framework/README.md) | How ML design differs from classic system design, the question families, the seven-step framework, scoping a vague question, common mistakes | `Chapter15-Notes.pdf` |
| 16 | [ML in Production](chapters/16-ml-in-production/README.md) | Training vs serving, training data, feature stores, batch/online/streaming inference, serving, offline metrics in production, drift and retraining, A/B tests, MLOps, an ETA capstone | `Chapter16-Notes.pdf` |
| 17 | [ML Case Studies I](chapters/17-ml-case-studies-1/README.md) | Video recommendations, social feed ranking, e-commerce search ranking and text-to-video search, each worked through one template | `Chapter17-Notes.pdf` |
| 18 | [ML Case Studies II](chapters/18-ml-case-studies-2/README.md) | Ad click prediction, payment fraud, harmful-content detection, People You May Know, music discovery, visual search, privacy blurring in street imagery, event recommendation, similar listings | `Chapter18-Notes.pdf` |
| 19 | [Responsible ML](chapters/19-responsible-ml/README.md) | Sources of bias, fairness definitions and measurement, mitigation, explainability, privacy, attacks on models, model cards, regulation, a checklist | `Chapter19-Notes.pdf` |
| **Part VI — Revision** | | | |
| 20 | [Cheat Sheets and Glossary](chapters/20-cheatsheet-and-glossary/README.md) | One-page sheets per topic, every algorithm and metric in a table, decision tables, the top 50 interview questions, and a 340+ term glossary linked to the chapters | `Chapter20-Notes.pdf` |

Each chapter's PDF sits next to its README: `chapters/NN-name/ChapterNN-Notes.pdf`.

## Colour code (in the PDFs)
Section headings are coloured by their role, so you can scan a page for the part you need:

| Colour | Headings |
|---|---|
| Green | **What it is**, analogies, overviews |
| Red / coral | **The problem it solves**, requirements |
| Blue | **How it works**, walkthroughs |
| Amber | **Trade-offs**, comparisons, **When not to use it** |
| Bright red | **Common mistakes**, pitfalls |
| Violet | **Real-world examples** |
| Pink | **In the interview**, 30-second answers (boxed), follow-ups |
| Teal | **Self-check** |
| Yellow | **TL;DR** (boxed) |
| Light blue | Key terms, "before you start" notes |

Code blocks are syntax-highlighted, and Mermaid diagrams are rendered as images.

## Running the code
All scripts use the shared virtual environment `../.venv-books` (Python 3.14 with numpy, pandas, scikit-learn 1.9, scipy, matplotlib and PyTorch, CPU only). Scripts need no network access and no API keys, use fixed random seeds, and run on a laptop CPU (most in a few seconds; the whole book takes several minutes).

```bash
# from ml/ — run every example in the book (prints OK / FAIL / SKIP per script)
bash ../tools_run_python.sh ml

# run one chapter's script (run it from its code/ folder, because scripts write plots to ../img/)
cd chapters/06-evaluation-and-tuning/code
"../../../../.venv-books/bin/python" <script>.py
```
Library versions change small numbers (the last digits of a score, a timing); the conclusions drawn in the text should not change. A script that is deliberately not standalone can opt out of the runner with a `# tools_run_python.sh: skip` comment.

## Rebuilding the PDFs
Requires Node 18+ and Google Chrome (the path is set in `tools/md2pdf.mjs`).

```bash
bash tools/regen_pdfs.sh      # every ChapterNN-Notes.pdf + ML-Handbook.pdf (cover title "Machine Learning Handbook")

# one chapter only
node tools/md2pdf.mjs chapters/06-evaluation-and-tuning/Chapter06-Notes.pdf chapters/06-evaluation-and-tuning/README.md
```
`md2pdf.mjs` reports any Mermaid errors or broken images.

## Sources
- **Courses:** DesignGurus, *Grokking Modern AI Fundamentals* and *Grokking the AI System Design Interview*. Their concepts, examples and case studies are covered in my own words; no course text is reproduced, and wording was checked against the course with an automated overlap test.
- **Books:** Aurélien Géron, *Hands-On Machine Learning with Scikit-Learn, Keras and TensorFlow*; James, Witten, Hastie and Tibshirani, *An Introduction to Statistical Learning*; Chip Huyen, *Designing Machine Learning Systems*; Ali Aminian and Alex Xu, *Machine Learning System Design Interview*; Goodfellow, Bengio and Courville, *Deep Learning*; Sutton and Barto, *Reinforcement Learning: An Introduction*.
- **Documentation and papers:** the scikit-learn user guide and API reference, PyTorch, NumPy, pandas and matplotlib docs, and the original papers for key methods (for example random forests, gradient boosting, dropout, batch normalization, attention and the transformer, DQN).
- **Web:** used to check facts and version details (library behaviour and deprecations, published numbers from real systems).
