package com.nx.timer;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Manages a stopwatch/countdown timer with start, pause, reset, and lap.
 * Uses a Handler for periodic UI updates every ~50ms for centisecond display.
 */
public class TimerManager {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final TextView display;
    private long elapsedMillis;
    private long startTime;
    private boolean isRunning;
    private boolean countdownMode;
    private long targetMillis = 60_000L; // default 1 menit
    private boolean targetReached;
    private final List<String> laps = new ArrayList<>();
    private OnTargetReachedListener targetReachedListener;
    private OnTickListener tickListener;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (countdownMode) {
                long now = System.currentTimeMillis();
                long remaining = targetMillis - (now - startTime);
                if (remaining <= 0) {
                    elapsedMillis = 0;
                    display.setText(formatTime(0));
                    isRunning = false;
                    targetReached = true;
                    if (targetReachedListener != null) {
                        targetReachedListener.onTargetReached();
                    }
                    if (tickListener != null) {
                        tickListener.onTick(0, targetMillis);
                    }
                    return;
                }
                elapsedMillis = remaining;
            } else {
                elapsedMillis = System.currentTimeMillis() - startTime;
            }
            display.setText(formatTime(elapsedMillis));
            if (tickListener != null) {
                tickListener.onTick(elapsedMillis, countdownMode ? targetMillis : 0);
            }
            handler.postDelayed(this, 50);
        }
    };

    public TimerManager(TextView display) {
        this.display = display;
        this.elapsedMillis = 0;
        this.isRunning = false;
        this.countdownMode = false;
        this.targetReached = false;
    }

    public void setOnTargetReachedListener(OnTargetReachedListener listener) {
        this.targetReachedListener = listener;
    }

    public void setOnTickListener(OnTickListener listener) {
        this.tickListener = listener;
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;
        targetReached = false;
        if (countdownMode) {
            startTime = System.currentTimeMillis();
            if (elapsedMillis <= 0) {
                elapsedMillis = targetMillis;
            }
        } else {
            startTime = System.currentTimeMillis() - elapsedMillis;
        }
        handler.post(tick);
    }

    public void pause() {
        if (!isRunning) return;
        isRunning = false;
        handler.removeCallbacks(tick);
    }

    public void reset() {
        if (isRunning) {
            isRunning = false;
            handler.removeCallbacks(tick);
        }
        elapsedMillis = countdownMode ? targetMillis : 0;
        targetReached = false;
        laps.clear();
        display.setText(formatTime(elapsedMillis));
        if (tickListener != null) {
            tickListener.onTick(elapsedMillis, countdownMode ? targetMillis : 0);
        }
    }

    public void markLap() {
        laps.add(formatTime(elapsedMillis));
    }

    public List<String> getLaps() {
        return Collections.unmodifiableList(laps);
    }

    public String getCurrentTime() {
        return formatTime(elapsedMillis);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isCountdownMode() {
        return countdownMode;
    }

    public void setCountdownMode(boolean enabled) {
        if (isRunning) return;
        this.countdownMode = enabled;
        if (enabled) {
            if (targetMillis <= 0) targetMillis = 60_000L;
            elapsedMillis = targetMillis;
        } else {
            elapsedMillis = 0;
        }
        targetReached = false;
        display.setText(formatTime(elapsedMillis));
        if (tickListener != null) {
            tickListener.onTick(elapsedMillis, countdownMode ? targetMillis : 0);
        }
    }

    public void setTargetTime(long millis) {
        if (isRunning) return;
        this.targetMillis = Math.max(1000L, millis); // minimal 1 detik
        if (countdownMode) {
            elapsedMillis = targetMillis;
            display.setText(formatTime(elapsedMillis));
            if (tickListener != null) {
                tickListener.onTick(elapsedMillis, targetMillis);
            }
        }
    }

    /** Adjust target in millis (positive = tambah, negative = kurang). Clamped to 1s..24h. */
    public void adjustTarget(long deltaMillis) {
        if (isRunning) return;
        long newTarget = targetMillis + deltaMillis;
        if (newTarget < 1000L) newTarget = 1000L;
        if (newTarget > 86_400_000L) newTarget = 86_400_000L; // max 24 jam
        setTargetTime(newTarget);
    }

    public long getTargetMillis() {
        return targetMillis;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public boolean isTargetReached() {
        return targetReached;
    }

    /** Format as HH:MM:SS.cc */
    static String formatTime(long millis) {
        long totalCentis = millis / 10;
        long hours = totalCentis / 360_000L;
        long minutes = (totalCentis % 360_000L) / 6_000L;
        long seconds = (totalCentis % 6_000L) / 100L;
        long centis = totalCentis % 100L;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d.%02d",
                hours, minutes, seconds, centis);
    }
}