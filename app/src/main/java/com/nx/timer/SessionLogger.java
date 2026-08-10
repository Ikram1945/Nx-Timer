package com.nx.timer;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Locale;

/**
 * Menyimpan dan menghitung total durasi stopwatch per hari, minggu, dan bulan.
 * Data disimpan di SharedPreferences.
 */
public class SessionLogger {

    private static final String PREFS = "session_log";
    private static final String KEY_PREFIX = "session_";

    private final SharedPreferences prefs;

    public SessionLogger(Context context) {
        this.prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /**
     * Mode timer:
     * <ul>
     *   <li>{@link #MODE_STOPWATCH} — stopwatch manual, dicatat ke statistik</li>
     *   <li>{@link #MODE_COUNTDOWN} — countdown murni, TIDAK dicatat</li>
     *   <li>{@link #MODE_POMODORO} — template Pomodoro otomatis, TIDAK dicatat</li>
     * </ul>
     */
    public static final int MODE_STOPWATCH = 0;
    public static final int MODE_COUNTDOWN = 1;
    public static final int MODE_POMODORO  = 2;

    private int mode = MODE_STOPWATCH;

    // Batasi maksimum history item agar hemat storage/limit (misal maks 50 item terbaru)
    private static final int MAX_HISTORY_ITEMS = 50;

    /** Catat satu sesi stopwatch (dipanggil saat pause/reset setelah running).
     *  Sesi otomatis (countdown / pomodoro) tidak ikut dicatat agar statistik benar-benar bersih. */
    public void logSession(long durationMillis, int mode) {
        if (mode != MODE_STOPWATCH) return; // whitelist: hanya stopwatch manual
        if (durationMillis <= 0) return;
        
        java.util.Map<String, ?> allEntries = prefs.getAll();
        java.util.List<String> keys = new java.util.ArrayList<>();
        for (String k : allEntries.keySet()) {
            if (k.startsWith(KEY_PREFIX)) {
                keys.add(k);
            }
        }
        
        // Urutkan key (timestamp) secara ascending
        java.util.Collections.sort(keys);

        SharedPreferences.Editor editor = prefs.edit();
        
        // Jika sudah melebihi limit, hapus yang paling lama (head of list)
        while (keys.size() >= MAX_HISTORY_ITEMS) {
            String oldestKey = keys.remove(0);
            editor.remove(oldestKey);
        }

        String key = KEY_PREFIX + System.currentTimeMillis();
        editor.putLong(key, durationMillis);
        editor.apply();
    }

    /** Backward-compatible overload: anggap sebagai stopwatch manual. */
    public void logSession(long durationMillis) {
        logSession(durationMillis, MODE_STOPWATCH);
    }

    public long getTodayTotal() {
        long start = getStartOfDay();
        return sumFrom(start);
    }

    public long getThisWeekTotal() {
        long start = getStartOfWeek();
        return sumFrom(start);
    }

    public long getThisMonthTotal() {
        long start = getStartOfMonth();
        return sumFrom(start);
    }

    /**
     * Mengembalikan total durasi per hari untuk minggu ini (Senin sampai Minggu).
     * Index 0 = Senin, Index 6 = Minggu.
     */
    public long[] getDailyTotalsForCurrentWeek() {
        long[] daily = new long[7];
        long weekStart = getStartOfWeek();

        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(KEY_PREFIX)) {
                long ts = Long.parseLong(key.substring(KEY_PREFIX.length()));
                if (ts >= weekStart) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(ts);
                    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                    int index = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - 2; // Sen=0 ... Min=6
                    if (index >= 0 && index < 7) {
                        daily[index] += prefs.getLong(key, 0);
                    }
                }
            }
        }
        return daily;
    }

    private long sumFrom(long startMillis) {
        long total = 0;
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(KEY_PREFIX)) {
                long ts = Long.parseLong(key.substring(KEY_PREFIX.length()));
                if (ts >= startMillis) {
                    total += prefs.getLong(key, 0);
                }
            }
        }
        return total;
    }

    private long getStartOfDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getStartOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getStartOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static String formatDuration(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
}