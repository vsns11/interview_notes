"""From waveform to log-Mel spectrogram: the "image" speech models read.

Whisper-style front end: 16 kHz audio, 25 ms windows (400 samples) every
10 ms (160 samples), FFT -> power spectrum -> Mel filterbank (80 bands) ->
log. 30 s of audio becomes 80 x 3000 numbers. TTS systems usually predict a
Mel spectrogram (or discrete audio-codec tokens) and a vocoder turns it back
into a waveform.

We synthesise a 2-second "voice-like" signal (a rising pitch with harmonics,
then a noisy "s" sound) so no audio file is needed.

Run:  python code/log_mel_spectrogram.py
"""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

IMG = Path(__file__).resolve().parent.parent / "img"
SR, N_FFT, HOP, N_MELS = 16_000, 400, 160, 80
rng = np.random.default_rng(0)


def hz_to_mel(f):
    return 2595 * np.log10(1 + f / 700)          # the common HTK formula


def mel_to_hz(m):
    return 700 * (10 ** (m / 2595) - 1)


def mel_filterbank(sr=SR, n_fft=N_FFT, n_mels=N_MELS):
    """Triangular filters evenly spaced on the Mel scale (dense at low Hz).
    Weights are computed from each FFT bin's exact frequency, so even the
    narrow low-frequency filters touch at least one bin."""
    hz = mel_to_hz(np.linspace(hz_to_mel(0), hz_to_mel(sr / 2), n_mels + 2))
    f = np.linspace(0, sr / 2, n_fft // 2 + 1)                     # FFT bin centres
    fb = np.zeros((n_mels, len(f)))
    for m in range(1, n_mels + 1):
        rising = (f - hz[m - 1]) / (hz[m] - hz[m - 1])
        falling = (hz[m + 1] - f) / (hz[m + 1] - hz[m])
        fb[m - 1] = np.maximum(0, np.minimum(rising, falling))
    return fb


def log_mel(wave):
    window = np.hanning(N_FFT)
    frames = [wave[i:i + N_FFT] * window for i in range(0, len(wave) - N_FFT + 1, HOP)]
    power = np.abs(np.fft.rfft(np.array(frames), axis=1)) ** 2       # (frames, 201)
    mel = power @ mel_filterbank().T                                  # (frames, 80)
    S = np.log10(np.maximum(mel, 1e-10)).T                            # (80, frames)
    return np.maximum(S, S.max() - 8.0)       # Whisper also clips to 8 decades below max


if __name__ == "__main__":
    t = np.arange(int(1.4 * SR)) / SR
    f0 = 120 + 80 * t                                                 # pitch 120 -> 232 Hz
    phase = 2 * np.pi * np.cumsum(f0) / SR
    voiced = sum(np.sin(k * phase) / k for k in range(1, 15))         # harmonics
    hiss = rng.standard_normal(int(0.6 * SR))
    hiss = np.convolve(hiss, [1, -1], mode="same")                    # high-pass: "sss"
    wave = np.concatenate([voiced / 3, 0.3 * hiss])
    S = log_mel(wave)
    print(f"waveform: {len(wave)} samples ({len(wave) / SR:.1f} s at {SR} Hz)")
    print(f"log-Mel spectrogram: {S.shape[0]} Mel bands x {S.shape[1]} frames")
    print(f"30 s of audio would give {N_MELS} x {30 * SR // HOP} frames (Whisper's input size)")
    edges = mel_to_hz(np.linspace(0, hz_to_mel(SR / 2), N_MELS + 2))
    print(f"low Mel filters are spaced {edges[2] - edges[1]:.0f} Hz apart, high ones "
          f"{edges[-1] - edges[-2]:.0f} Hz apart (ears resolve low pitches finely)")

    fig, axes = plt.subplots(2, 1, figsize=(9, 5.5))
    axes[0].plot(np.arange(len(wave)) / SR, wave, lw=0.3)
    axes[0].set_title("waveform: voiced sound with rising pitch, then a hiss")
    axes[0].set_xlabel("seconds")
    im = axes[1].imshow(S, origin="lower", aspect="auto", cmap="magma",
                        extent=[0, len(wave) / SR, 0, N_MELS])
    axes[1].set_title("log-Mel spectrogram (what Whisper-style encoders read)")
    axes[1].set_xlabel("seconds"); axes[1].set_ylabel("Mel band")
    fig.colorbar(im, ax=axes[1], label="log10 power")
    fig.tight_layout()
    IMG.mkdir(exist_ok=True)
    fig.savefig(IMG / "log_mel_spectrogram.png", dpi=110)
    print("saved img/log_mel_spectrogram.png")
