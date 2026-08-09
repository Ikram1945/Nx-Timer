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
        var btnFocusMode = (Button) view.findViewById(R.id.btn_focus_mode);
        var btnShare = (Button) view.findViewById(R.id.btn_share);

        timerUiController.setup(btnStart, btnPause, btnReset, btnCopy, btnLap, btnCountdown,
                btnMinus, btnPlus, display);
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }
}