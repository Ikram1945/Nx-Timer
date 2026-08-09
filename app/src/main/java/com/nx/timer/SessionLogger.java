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

    /** Catat satu sesi stopwatch (dipanggil saat pause/reset setelah running). */
    public void logSession(long durationMillis) {
        if (durationMillis <= 0) return;
        String key = KEY_PREFIX + System.currentTimeMillis();
        prefs.edit().putLong(key, durationMillis).apply();
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