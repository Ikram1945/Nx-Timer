package com.nx.timer;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private TimerFragment timerFragment;
    private AlarmFragment alarmFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View root = findViewById(android.R.id.content);
        ClickAnimator.applyToAll(root);

        timerFragment = new TimerFragment();
        alarmFragment = new AlarmFragment();

        // Show timer fragment by default
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, alarmFragment, "ALARM")
                .hide(alarmFragment)
                .add(R.id.fragment_container, timerFragment, "TIMER")
                .commit();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_timer) {
                getSupportFragmentManager().beginTransaction()
                        .hide(alarmFragment)
                        .show(timerFragment)
                        .commit();
                return true;
            } else if (item.getItemId() == R.id.nav_alarm) {
                getSupportFragmentManager().beginTransaction()
                        .hide(timerFragment)
                        .show(alarmFragment)
                        .commit();
                return true;
            }
            return false;
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            // Check which fragment is visible and add lap if timer
            if (timerFragment.isVisible() && timerFragment.getTimerManager() != null) {
                timerFragment.getTimerManager().markLap();
                Snackbar.make(v, R.string.lap_saved, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(v, R.string.fab_clicked, Snackbar.LENGTH_SHORT).show();
            }
        });

        var toolbar = (com.google.android.material.appbar.MaterialToolbar) findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_about_me) {
                showAboutDialog();
                return true;
            }
            return false;
        });
    }

    private void showAboutDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_about, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        view.findViewById(R.id.btn_about_close).setOnClickListener(v -> dialog.dismiss());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        dialog.show();
    }
}