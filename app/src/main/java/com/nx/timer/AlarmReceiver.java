package com.nx.timer;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "alarm_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Acquire wake lock agar CPU tetap hidup selama proses alarm
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        if (pm != null) {
            wakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.ON_AFTER_RELEASE,
                    "timer:alarm_wakelock");
            wakeLock.acquire(10_000); // 10 detik timeout
        }

        try {
            String label = intent.getStringExtra("label");
            if (label == null) label = "Alarm";

            int hour = intent.getIntExtra("hour", -1);
            int minute = intent.getIntExtra("minute", -1);
            int day = intent.getIntExtra("day", -1);

            // Reschedule untuk minggu depan (repeat)
            if (hour >= 0 && minute >= 0 && day >= 0) {
                rescheduleForNextWeek(context, hour, minute, day, label);
            }

            // 1. Buka full-screen alarm activity
            Intent alarmIntent = new Intent(context, AlarmAlertActivity.class);
            alarmIntent.putExtra("label", label);
            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            try {
                context.startActivity(alarmIntent);
            } catch (Exception ignored) {
                // Background activity start restriction — fallback ke notifikasi saja
            }

            // 2. Tampilkan notifikasi
            createNotificationChannel(context);
            showNotification(context, label, alarmIntent);
        } finally {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    private void rescheduleForNextWeek(Context context, int hour, int minute, int day, String label) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("label", label);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);
        intent.putExtra("day", day);
        int requestCode = (day * 10000 + hour * 100 + minute);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, 7); // minggu depan

        scheduleExact(context, alarmMgr, calendar.getTimeInMillis(), pendingIntent, label);
    }

    private void showNotification(Context context, String label, Intent alarmIntent) {
        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent fullScreenIntent = PendingIntent.getActivity(
                context, 1, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("⏰ " + label)
                .setContentText("Waktunya!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setOngoing(true)
                .setFullScreenIntent(fullScreenIntent, true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;
            // Cek kalau channel sudah ada, skip
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return;

            CharSequence name = "Alarm";
            String description = "Channel untuk notifikasi alarm";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            channel.setSound(alarmUri, attrs);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000, 500, 1000});

            manager.createNotificationChannel(channel);
        }
    }

    /** Jadwalkan alarm dengan metode paling akurat yang tersedia */
    static void scheduleExact(Context context, AlarmManager alarmMgr, long triggerTime,
                              PendingIntent operation, String label) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmMgr.canScheduleExactAlarms()) {
                // Jangan jadwalkan dengan set() inexact — biarkan gagal agar user tahu perlu izin
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent showIntent = new Intent(context, AlarmAlertActivity.class);
            showIntent.putExtra("label", label);
            PendingIntent showPendingIntent = PendingIntent.getActivity(
                    context, 9999, showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager.AlarmClockInfo alarmClockInfo =
                    new AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent);
            alarmMgr.setAlarmClock(alarmClockInfo, operation);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, operation);
        } else {
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, triggerTime, operation);
        }
    }
}