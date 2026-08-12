package com.nx.timer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

/**
 * Menampilkan notifikasi berjalan (foreground) selama timer aktif.
 * Menampilkan sisa waktu / waktu berjalan agar terlihat dari notification shade.
 */
public class TimerForegroundService extends Service {

    private static final String CHANNEL_ID = "timer_running_channel";
    private static final int NOTIFICATION_ID = 1001;

    public static final String EXTRA_TEXT = "extra_timer_text";
    public static final String EXTRA_MODE = "extra_timer_mode";

    public static final String ACTION_UPDATE = "com.nx.timer.action.UPDATE_TIMER";
    public static final String ACTION_STOP = "com.nx.timer.action.STOP_TIMER";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String text = intent != null && intent.getStringExtra(EXTRA_TEXT) != null
                ? intent.getStringExtra(EXTRA_TEXT)
                : "Timer berjalan";
        String mode = intent != null && intent.getStringExtra(EXTRA_MODE) != null
                ? intent.getStringExtra(EXTRA_MODE)
                : "Stopwatch";

        startForeground(NOTIFICATION_ID, buildNotification(mode, text));
        return START_NOT_STICKY;
    }

    private Notification buildNotification(String title, String text) {
        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setContentIntent(contentIntent);

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager == null) return;
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return;

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Timer Berjalan", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Menampilkan status timer yang sedang berjalan");
            channel.setShowBadge(false);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
