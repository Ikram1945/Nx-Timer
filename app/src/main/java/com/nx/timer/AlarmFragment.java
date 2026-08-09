package com.nx.timer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmFragment extends Fragment {

    private TimePicker timePicker;
    private com.google.android.material.textfield.TextInputEditText labelInput;
    private LinearLayout alarmListContainer;
    private TextView alarmEmpty;
    private final List<Chip> dayChips = new ArrayList<>();
    private final List<AlarmItem> alarms = new ArrayList<>();
    private AlarmStorage alarmStorage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alarm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ClickAnimator.applyToAll(view);

        timePicker = view.findViewById(R.id.alarm_time_picker);
        labelInput = view.findViewById(R.id.alarm_label);
        alarmListContainer = view.findViewById(R.id.alarm_list_container);
        alarmEmpty = view.findViewById(R.id.alarm_empty);
        Button btnSetAlarm = view.findViewById(R.id.btn_set_alarm);

        dayChips.add((Chip) view.findViewById(R.id.chip_sen));
        dayChips.add((Chip) view.findViewById(R.id.chip_sel));
        dayChips.add((Chip) view.findViewById(R.id.chip_rab));
        dayChips.add((Chip) view.findViewById(R.id.chip_kam));
        dayChips.add((Chip) view.findViewById(R.id.chip_jum));
        dayChips.add((Chip) view.findViewById(R.id.chip_sab));
        dayChips.add((Chip) view.findViewById(R.id.chip_min));

        btnSetAlarm.setOnClickListener(v -> setAlarm());

        alarmStorage = new AlarmStorage(requireContext());
        alarms.addAll(alarmStorage.loadAlarms());
        refreshAlarmList();

        // Predictive suggestion
        showPredictiveSuggestion(view);
    }

    private void showPredictiveSuggestion(View view) {
        LinearLayout suggestionContainer = view.findViewById(R.id.suggestion_container);
        if (suggestionContainer == null) return;

        suggestionContainer.removeAllViews();

        if (alarms.isEmpty()) return;

        // Hitung frekuensi jam alarm
        java.util.Map<String, Integer> freq = new java.util.HashMap<>();
        for (AlarmItem a : alarms) {
            String key = String.format("%02d:%02d", a.getHour(), a.getMinute());
            freq.put(key, freq.getOrDefault(key, 0) + 1);
        }

        // Ambil yang paling sering
        String mostFrequent = null;
        int max = 0;
        for (java.util.Map.Entry<String, Integer> e : freq.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                mostFrequent = e.getKey();
            }
        }

        if (mostFrequent != null && max >= 1) {
            final String frequentTime = mostFrequent;
            TextView suggestion = new TextView(requireContext());
            suggestion.setText("💡 Kamu biasanya alarm jam " + frequentTime + ". Set lagi?");
            suggestion.setTextSize(13f);
            suggestion.setTextColor(0xFF6366F1);
            suggestion.setPadding(16, 8, 16, 8);
            suggestion.setOnClickListener(v -> {
                String[] parts = frequentTime.split(":");
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    timePicker.setHour(h);
                    timePicker.setMinute(m);
                } else {
                    timePicker.setCurrentHour(h);
                    timePicker.setCurrentMinute(m);
                }
                Snackbar.make(requireView(), "Waktu alarm disesuaikan ke " + frequentTime, Snackbar.LENGTH_SHORT).show();
            });
            suggestionContainer.addView(suggestion);
        }
    }

    private void setAlarm() {
        String label = labelInput.getText() != null ? labelInput.getText().toString().trim() : "";
        if (label.isEmpty()) {
            Snackbar.make(requireView(), R.string.alarm_validation, Snackbar.LENGTH_SHORT).show();
            return;
        }

        boolean hasDay = false;
        for (Chip chip : dayChips) {
            if (chip.isChecked()) {
                hasDay = true;
                break;
            }
        }
        if (!hasDay) {
            Snackbar.make(requireView(), R.string.alarm_validation, Snackbar.LENGTH_SHORT).show();
            return;
        }

        int hour, minute;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
        } else {
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
        }

        List<Integer> repeatDays = new ArrayList<>();
        for (int i = 0; i < dayChips.size(); i++) {
            if (dayChips.get(i).isChecked()) {
                repeatDays.add(i);
            }
        }

        scheduleSystemAlarms(hour, minute, repeatDays, label);

        AlarmItem item = new AlarmItem(hour, minute, label, repeatDays);
        alarms.add(item);
        alarmStorage.saveAlarms(alarms);
        refreshAlarmList();

        labelInput.setText("");
        for (Chip chip : dayChips) {
            chip.setChecked(false);
        }

        Snackbar.make(requireView(), R.string.alarm_set_success, Snackbar.LENGTH_SHORT).show();
    }

    private void scheduleSystemAlarms(int hour, int minute, List<Integer> repeatDays, String label) {
        Context ctx = requireContext();
        AlarmManager alarmMgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr == null) return;

        for (int targetDay : repeatDays) {
            scheduleSingleDayAlarm(ctx, alarmMgr, hour, minute, targetDay, label);
        }
    }

    private void scheduleSingleDayAlarm(Context ctx, AlarmManager alarmMgr,
                                        int hour, int minute, int targetDay, String label) {
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.putExtra("label", label);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);
        intent.putExtra("day", targetDay);
        int requestCode = (targetDay * 10000 + hour * 100 + minute);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ctx, requestCode, intent,
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

        AlarmReceiver.scheduleExact(ctx, alarmMgr, calendar.getTimeInMillis(), pendingIntent, label);
    }

    private void refreshAlarmList() {
        for (int i = alarmListContainer.getChildCount() - 1; i >= 0; i--) {
            View child = alarmListContainer.getChildAt(i);
            if (child != alarmEmpty) {
                alarmListContainer.removeViewAt(i);
            }
        }

        if (alarms.isEmpty()) {
            alarmEmpty.setVisibility(View.VISIBLE);
        } else {
            alarmEmpty.setVisibility(View.GONE);
            for (int i = 0; i < alarms.size(); i++) {
                AlarmItem item = alarms.get(i);
                View itemView = createAlarmItemView(item, i);
                alarmListContainer.addView(itemView);
            }
        }
    }

    private View createAlarmItemView(AlarmItem item, int index) {
        TextView tv = new TextView(requireContext());
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setPadding(24, 16, 24, 16);
        tv.setBackgroundResource(R.drawable.bg_button_soft);

        StringBuilder sb = new StringBuilder();
        sb.append("⏰ ").append(String.format("%02d:%02d", item.getHour(), item.getMinute()));
        sb.append("  —  ").append(item.getLabel());
        if (!item.getRepeatDays().isEmpty()) {
            sb.append("\nUlang: ");
            String[] dayNames = {"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};
            for (int day : item.getRepeatDays()) {
                sb.append(dayNames[day]).append(" ");
            }
        }
        tv.setText(sb.toString());
        tv.setTextColor(0xFF1F2933);
        tv.setTextSize(14);

        tv.setOnLongClickListener(v -> {
            cancelAlarmForItem(item);
            alarms.remove(index);
            alarmStorage.saveAlarms(alarms);
            refreshAlarmList();
            Snackbar.make(requireView(), "Alarm dihapus", Snackbar.LENGTH_SHORT).show();
            return true;
        });

        return tv;
    }

    private void cancelAlarmForItem(AlarmItem item) {
        Context ctx = requireContext();
        AlarmManager alarmMgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr == null) return;

        for (int day : item.getRepeatDays()) {
            Intent intent = new Intent(ctx, AlarmReceiver.class);
            int requestCode = (day * 10000 + item.getHour() * 100 + item.getMinute());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    ctx, requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmMgr.cancel(pendingIntent);
        }
    }
}