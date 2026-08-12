package com.nx.timer;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private TimerFragment timerFragment;
    private AlarmFragment alarmFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Minta izin SCHEDULE_EXACT_ALARM jika belum diberikan (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        // Minta izin POST_NOTIFICATIONS (Android 13+) agar notifikasi berjalan tampil
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

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

        var toolbar = (com.google.android.material.appbar.MaterialToolbar) findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_dark_mode) {
                toggleDarkMode();
                return true;
            } else if (item.getItemId() == R.id.action_about_me) {
                showAboutDialog();
                return true;
            }
            return false;
        });
    }

    private void toggleDarkMode() {
        android.content.SharedPreferences prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        boolean newMode = !isDarkMode;

        prefs.edit().putBoolean("dark_mode", newMode).apply();

        if (newMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }
        recreate();
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