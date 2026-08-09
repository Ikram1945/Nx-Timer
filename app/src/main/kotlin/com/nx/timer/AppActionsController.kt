package com.nx.timer

import android.app.Activity
import android.content.Intent
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

internal class AppActionsController(private val activity: Activity) {

    private var focusModeEnabled = false

    fun setup(
        fab: FloatingActionButton,
        btnFocusMode: Button,
        btnShare: Button,
        timerManager: TimerManager
    ) {
        fab.setOnClickListener {
            Snackbar.make(it, R.string.fab_clicked, Snackbar.LENGTH_SHORT).show()
        }

        btnFocusMode.setOnClickListener {
            focusModeEnabled = !focusModeEnabled
            Snackbar.make(
                it,
                if (focusModeEnabled) R.string.focus_mode_on else R.string.focus_mode_off,
                Snackbar.LENGTH_SHORT
            ).show()
        }

        btnShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.share_subject))
                putExtra(
                    Intent.EXTRA_TEXT,
                    activity.getString(R.string.share_template, timerManager.getCurrentTime())
                )
            }
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.btn_share)))
        }
    }
}