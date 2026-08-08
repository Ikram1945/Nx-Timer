package com.nx.timer;

import android.content.Intent;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

final class AppActionsController {

    private final android.app.Activity activity;
    private boolean focusModeEnabled;

    AppActionsController(android.app.Activity activity) {
        this.activity = activity;
    }

    void setup(FloatingActionButton fab, Button btnFocusMode, Button btnShare, TimerManager timerManager) {
        fab.setOnClickListener(view ->
                Snackbar.make(view, R.string.fab_clicked, Snackbar.LENGTH_SHORT).show()
        );

        btnFocusMode.setOnClickListener(v -> {
            focusModeEnabled = !focusModeEnabled;
            Snackbar.make(v, focusModeEnabled ? R.string.focus_mode_on : R.string.focus_mode_off, Snackbar.LENGTH_SHORT).show();
        });

        btnShare.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.share_subject));
            intent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.share_template, timerManager.getCurrentTime()));
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.btn_share)));
        });
    }
}