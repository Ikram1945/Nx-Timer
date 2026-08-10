package com.nx.timer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TimerFragment extends Fragment {

    private TimerManager timerManager;
    private TimerUiController timerUiController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ClickAnimator.applyToAll(view);

        var display = (TextView) view.findViewById(R.id.timer_display);
        var timerStatus = (TextView) view.findViewById(R.id.timer_status);
        var lastLap = (TextView) view.findViewById(R.id.timer_last_lap);
        var progressBar = (ProgressBar) view.findViewById(R.id.timer_progress);
        var lapListText = (TextView) view.findViewById(R.id.lap_list);
        var targetLabel = (TextView) view.findViewById(R.id.target_label);

        timerManager = new TimerManager(display);

        var activity = requireActivity();
        SessionLogger logger = new SessionLogger(activity);
        timerManager.setSessionLogger(logger);

        timerUiController = new TimerUiController(activity, timerManager, timerStatus, lastLap,
                progressBar, lapListText, targetLabel);

        var btnStart = (Button) view.findViewById(R.id.btn_start);
        var btnPause = (Button) view.findViewById(R.id.btn_pause);
        var btnReset = (Button) view.findViewById(R.id.btn_reset);
        var btnCopy = (Button) view.findViewById(R.id.btn_copy);
        var btnLap = (Button) view.findViewById(R.id.btn_lap);
        var btnCountdown = (Button) view.findViewById(R.id.btn_countdown);
        var btnMinus = (Button) view.findViewById(R.id.btn_minus);
        var btnPlus = (Button) view.findViewById(R.id.btn_plus);
        var btnPomodoro = (Button) view.findViewById(R.id.btn_pomodoro);

        timerUiController.setup(btnStart, btnPause, btnReset, btnCopy, btnLap, btnCountdown,
                btnMinus, btnPlus, display);

        var cycleCounterCard = view.findViewById(R.id.cycle_counter_card);
        var cycleCountDisplay = (TextView) view.findViewById(R.id.cycle_count_display);
        var btnCycleReset = (Button) view.findViewById(R.id.btn_cycle_reset);

        timerUiController.setupCycle(cycleCounterCard, cycleCountDisplay, btnCycleReset);

        // Built-in template: 25 menit kerja + 5 menit istirahat
        btnPomodoro.setOnClickListener(v -> {
            timerManager.setTemplate(com.nx.timer.TimerTemplate.Companion.getPOMODORO_25_5());
            timerManager.setPomodoroMode(true);
            timerUiController.updateCountdownButton(btnCountdown);
            timerUiController.updateTargetLabel();
            timerUiController.updateProgress();
            timerUiController.updateTimerStatus();
        });

        // Tampilkan statistik durasi
        updateStats(view, logger);
        updateChart(view, logger);
    }

    private void updateStats(View view, SessionLogger logger) {
        TextView today = view.findViewById(R.id.stat_today);
        TextView week = view.findViewById(R.id.stat_week);
        TextView month = view.findViewById(R.id.stat_month);

        today.setText(SessionLogger.formatDuration(logger.getTodayTotal()));
        week.setText(SessionLogger.formatDuration(logger.getThisWeekTotal()));
        month.setText(SessionLogger.formatDuration(logger.getThisMonthTotal()));
    }

    private void updateChart(View view, SessionLogger logger) {
        DurationBarChart chart = view.findViewById(R.id.duration_chart);
        long[] daily = logger.getDailyTotalsForCurrentWeek();

        List<DurationBarChart.BarData> chartData = new java.util.ArrayList<>();
        String[] labels = {"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};
        for (int i = 0; i < 7; i++) {
            float hours = daily[i] / 3600000f;
            chartData.add(new DurationBarChart.BarData(labels[i], hours));
        }
        chart.setData(chartData);
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }
}