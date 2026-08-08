package com.nx.timer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleAllAlarms(context);
        }
    }

    public static void rescheduleAllAlarms(Context context) {
        AlarmStorage storage = new AlarmStorage(context);
        List<AlarmItem> alarms = storage.loadAlarms();

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr == null) return;

        for (AlarmItem item : alarms) {
            for (int day : item.repeatDays) {
                scheduleSingle(context, alarmMgr, item.hour, item.minute, day, item.label);
            }
        }
    }

    private static void scheduleSingle(Context context, AlarmManager alarmMgr,
                                       int hour, int minute, int targetDay, String label) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("label", label);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);
        intent.putExtra("day", targetDay);
        int requestCode = (targetDay * 10000 + hour * 100 + minute);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        int currentIndex = (currentDay == Calendar.SUNDAY) ? 6 : currentDay - 2;

        int daysUntilTarget = targetDay - currentIndex;
        if (daysUntilTarget < 0) {
            daysUntilTarget += 7;
        } else if (daysUntilTarget == 0) {
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                daysUntilTarget = 7;
            }
        }
        calendar.add(Calendar.DAY_OF_YEAR, daysUntilTarget);

        AlarmReceiver.scheduleExact(context, alarmMgr, calendar.getTimeInMillis(), pendingIntent, label);
    }
}