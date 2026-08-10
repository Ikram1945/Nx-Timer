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
    private SessionLogger sessionLogger;

    // --- Built-in template (Pomodoro 25:5) ---
    private boolean pomodoroMode = false;
    private boolean isWorkPhase = true; // true = fase kerja, false = fase istirahat
    private long workMillis = 25 * 60_000L;
    private long breakMillis = 5 * 60_000L;
    private int cycleCount = 0;

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
                    cycleCount++;
                    // Built-in template: toggle otomatis fase kerja <-> istirahat
                    if (pomodoroMode) {
                        isWorkPhase = !isWorkPhase;
                        targetMillis = isWorkPhase ? workMillis : breakMillis;
                    }
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

    public void setSessionLogger(SessionLogger logger) {
        this.sessionLogger = logger;
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
        if (sessionLogger != null) {
            // Hanya stopwatch manual yang ikut statistik; countdown & pomodoro di-skip.
            sessionLogger.logSession(elapsedMillis, getSessionMode());
        }
    }

    /** Mode yang dipakai saat pencatatan sesi (whitelist: hanya stopwatch manual yang dicatat).
     *  Konstanta mengikuti {@link SessionLogger}: 0 = stopwatch, 1 = countdown, 2 = pomodoro. */
    public int getSessionMode() {
        if (pomodoroMode) return 2; // SessionLogger.MODE_POMODORO
        if (countdownMode) return 1; // SessionLogger.MODE_COUNTDOWN
        return 0;                    // SessionLogger.MODE_STOPWATCH
    }

    public void reset() {
        if (isRunning) {
            isRunning = false;
            handler.removeCallbacks(tick);
        }
        // Built-in template: reset ke fase kerja
        if (pomodoroMode) {
            isWorkPhase = true;
            targetMillis = workMillis;
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
        laps.clear(); // FIX: bersihkan lap setiap ganti mode
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

    /**
     * Aktifkan/non-aktifkan built-in template Pomodoro (25 menit kerja + 5 menit istirahat).
     * Saat diaktifkan, timer otomatis beralih antara fase kerja dan istirahat.
     */
    public void setPomodoroMode(boolean enabled) {
        if (isRunning) return;
        this.pomodoroMode = enabled;
        if (enabled) {
            countdownMode = true;
            isWorkPhase = true;
            targetMillis = workMillis;
            elapsedMillis = targetMillis;
            targetReached = false;
            laps.clear(); // FIX: bersihkan lap lama agar tidak ikut terhitung di mode Pomodoro
            display.setText(formatTime(elapsedMillis));
            if (tickListener != null) {
                tickListener.onTick(elapsedMillis, targetMillis);
            }
        } else {
            // Reset ke mode countdown default
            setCountdownMode(true);
        }
    }

    public boolean isPomodoroMode() {
        return pomodoroMode;
    }

    public boolean isWorkPhase() {
        return isWorkPhase;
    }

    public void setTemplate(com.nx.timer.TimerTemplate template) {
        this.workMillis = template.getWorkMillis();
        this.breakMillis = template.getBreakMillis();
        if (pomodoroMode) {
            targetMillis = isWorkPhase ? workMillis : breakMillis;
            if (!isRunning) {
                elapsedMillis = targetMillis;
                display.setText(formatTime(elapsedMillis));
            }
        }
    }

    public long getWorkMillis() { return workMillis; }
    public long getBreakMillis() { return breakMillis; }

    public int getCycleCount() {
        return cycleCount;
    }

    public void incrementCycle() {
        cycleCount++;
    }

    public void decrementCycle() {
        if (cycleCount > 0) {
            cycleCount--;
        }
    }

    public void resetCycle() {
        cycleCount = 0;
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