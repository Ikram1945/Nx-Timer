package com.nx.timer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

final class TimerUiController {

    private final android.app.Activity activity;
    private final TimerManager timerManager;
    private final TextView timerStatus;
    private final TextView lastLap;
    private final ProgressBar progressBar;
    private final TextView lapListText;
    private final TextView targetLabel;

    TimerUiController(android.app.Activity activity, TimerManager timerManager, TextView timerStatus,
                      TextView lastLap, ProgressBar progressBar, TextView lapListText,
                      TextView targetLabel) {
        this.activity = activity;
        this.timerManager = timerManager;
        this.timerStatus = timerStatus;
        this.lastLap = lastLap;
        this.progressBar = progressBar;
        this.lapListText = lapListText;
        this.targetLabel = targetLabel;
    }

    void setup(Button btnStart, Button btnPause, Button btnReset, Button btnCopy,
               Button btnLap, Button btnCountdown, Button btnMinus, Button btnPlus,
               TextView display) {
        btnStart.setOnClickListener(v -> {
            timerManager.start();
            updateTimerStatus();
        });
        btnPause.setOnClickListener(v -> {
            timerManager.pause();
            updateTimerStatus();
        });
        btnReset.setOnClickListener(v -> {
            timerManager.reset();
            updateTimerStatus();
            updateLastLap();
            updateLapList();
            updateProgress();
            updateTargetLabel();
        });
        btnCopy.setOnClickListener(v -> copyTimerValue(display.getText().toString()));
        btnLap.setOnClickListener(v -> {
            timerManager.markLap();
            updateLastLap();
            updateLapList();
        });
        btnCountdown.setOnClickListener(v -> {
            timerManager.setCountdownMode(!timerManager.isCountdownMode());
            updateCountdownButton(btnCountdown);
            updateTimerStatus();
            updateProgress();
            updateTargetLabel();
        });
        btnMinus.setOnClickListener(v -> {
            // kurangi 10 detik
            timerManager.adjustTarget(-10_000L);
            updateTargetLabel();
            updateProgress();
        });
        btnPlus.setOnClickListener(v -> {
            // tambah 10 detik
            timerManager.adjustTarget(10_000L);
            updateTargetLabel();
            updateProgress();
        });

        timerManager.setOnTickListener((elapsed, total) -> updateProgress());
        timerManager.setOnTargetReachedListener(this::onTargetReached);

        updateTimerStatus();
        updateLastLap();
        updateLapList();
        updateCountdownButton(btnCountdown);
        updateProgress();
        updateTargetLabel();
    }

    void updateTimerStatus() {
        if (timerManager.isTargetReached()) {
            timerStatus.setText(R.string.countdown_finished);
        } else if (timerManager.isRunning()) {
            if (timerManager.isCountdownMode()) {
                timerStatus.setText(R.string.countdown_running);
            } else {
                timerStatus.setText(R.string.timer_running);
            }
        } else if (timerManager.isCountdownMode()) {
            timerStatus.setText(R.string.countdown_ready);
        } else if (timerManager.getCurrentTime().equals("00:00:00.00")) {
            timerStatus.setText(R.string.timer_ready);
        } else {
            timerStatus.setText(R.string.timer_paused);
        }
    }

    void updateLastLap() {
        var laps = timerManager.getLaps();
        if (laps.isEmpty()) {
            lastLap.setText(R.string.timer_initial);
        } else {
            lastLap.setText(laps.get(laps.size() - 1));
            Snackbar.make(activity.findViewById(android.R.id.content),
                    activity.getString(R.string.lap_saved, lastLap.getText()),
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    void updateLapList() {
        var laps = timerManager.getLaps();
        if (laps.isEmpty()) {
            lapListText.setText(R.string.no_laps);
            lapListText.setVisibility(View.GONE);
        } else {
            StringBuilder sb = new StringBuilder();
            int start = Math.max(0, laps.size() - 7);
            for (int i = start; i < laps.size(); i++) {
                sb.append("Lap ").append(i + 1).append(": ").append(laps.get(i));
                if (i < laps.size() - 1) sb.append("\n");
            }
            lapListText.setText(sb.toString());
            lapListText.setVisibility(View.VISIBLE);
        }
    }

    void updateProgress() {
        if (timerManager.isCountdownMode() && timerManager.getTargetMillis() > 0) {
            long total = timerManager.getTargetMillis();
            long elapsed = timerManager.getElapsedMillis();
            int progress = (int) ((total - elapsed) * 100 / total);
            progressBar.setProgress(Math.max(0, Math.min(100, progress)));
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    void updateTargetLabel() {
        if (timerManager.isCountdownMode()) {
            long t = timerManager.getTargetMillis();
            long seconds = t / 1000;
            long mins = seconds / 60;
            long secs = seconds % 60;
            targetLabel.setText(String.format("Target: %d:%02d", mins, secs));
            targetLabel.setVisibility(View.VISIBLE);
            ((View) targetLabel.getParent()).setVisibility(View.VISIBLE);
        } else {
            targetLabel.setVisibility(View.GONE);
            ((View) targetLabel.getParent()).setVisibility(View.GONE);
        }
    }

    private void updateCountdownButton(Button btnCountdown) {
        if (timerManager.isCountdownMode()) {
            btnCountdown.setText(R.string.mode_countdown);
        } else {
            btnCountdown.setText(R.string.mode_stopwatch);
        }
    }

    private void onTargetReached() {
        Object svc = activity.getSystemService(Context.VIBRATOR_SERVICE);
        if (svc instanceof Vibrator) {
            Vibrator vibrator = (Vibrator) svc;
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500,
                            VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }
            }
        }
        Snackbar.make(activity.findViewById(android.R.id.content),
                R.string.countdown_finished, Snackbar.LENGTH_LONG).show();
        updateTimerStatus();
        updateProgress();
    }

    private void copyTimerValue(String value) {
        var clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Timer", value));
            Snackbar.make(activity.findViewById(android.R.id.content),
                    R.string.timer_copied, Snackbar.LENGTH_SHORT).show();
        }
    }
}