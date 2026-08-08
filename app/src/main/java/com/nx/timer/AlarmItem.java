package com.nx.timer;

import java.util.List;

class AlarmItem {
    final int hour;
    final int minute;
    final String label;
    final List<Integer> repeatDays;

    AlarmItem(int hour, int minute, String label, List<Integer> repeatDays) {
        this.hour = hour;
        this.minute = minute;
        this.label = label;
        this.repeatDays = repeatDays;
    }
}