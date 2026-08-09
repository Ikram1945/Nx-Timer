package com.nx.timer;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Menyimpan dan memuat alarm ke/dari SharedPreferences (JSON).
 * Data tetap ada meskipun app ditutup.
 */
public class AlarmStorage {

    private static final String PREFS_NAME = "alarm_prefs";
    private static final String KEY_ALARMS = "alarms";

    private final SharedPreferences prefs;

    public AlarmStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<AlarmItem> loadAlarms() {
        List<AlarmItem> list = new ArrayList<>();
        String json = prefs.getString(KEY_ALARMS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                int hour = obj.getInt("hour");
                int minute = obj.getInt("minute");
                String label = obj.getString("label");
                JSONArray daysArr = obj.getJSONArray("repeatDays");
                List<Integer> repeatDays = new ArrayList<>();
                for (int j = 0; j < daysArr.length(); j++) {
                    repeatDays.add(daysArr.getInt(j));
                }
                list.add(new AlarmItem(hour, minute, label, repeatDays));
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    public void saveAlarms(List<AlarmItem> alarms) {
        JSONArray arr = new JSONArray();
        try {
            for (AlarmItem item : alarms) {
                JSONObject obj = new JSONObject();
                obj.put("hour", item.getHour());
                obj.put("minute", item.getMinute());
                obj.put("label", item.getLabel());
                JSONArray daysArr = new JSONArray();
                for (int day : item.getRepeatDays()) {
                    daysArr.put(day);
                }
                obj.put("repeatDays", daysArr);
                arr.put(obj);
            }
        } catch (Exception ignored) {
        }
        prefs.edit().putString(KEY_ALARMS, arr.toString()).apply();
    }
}