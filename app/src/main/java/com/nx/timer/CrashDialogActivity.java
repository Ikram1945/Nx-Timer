package com.nx.timer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity shown when the app crashes. Offers options to copy the error,
 * contact the developer via WhatsApp, restart, or close.
 */
public class CrashDialogActivity extends AppCompatActivity {

    private String crashLog;
    private String crashMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_dialog);

        crashLog = getIntent().getStringExtra("crash_log");
        crashMessage = getIntent().getStringExtra("crash_message");

        if (crashLog == null) crashLog = "No crash log available.";
        if (crashMessage == null) crashMessage = "Unknown error";

        TextView tvMessage = findViewById(R.id.crash_message);
        TextView tvDetail = findViewById(R.id.crash_detail);
        Button btnCopy = findViewById(R.id.btn_crash_copy);
        Button btnWhatsApp = findViewById(R.id.btn_crash_whatsapp);
        Button btnRestart = findViewById(R.id.btn_crash_restart);
        Button btnClose = findViewById(R.id.btn_crash_close);

        tvMessage.setText(crashMessage);
        tvDetail.setText(crashLog);

        btnCopy.setOnClickListener(v -> copyToClipboard());
        btnWhatsApp.setOnClickListener(v -> openWhatsApp());
        btnRestart.setOnClickListener(v -> restartApp());
        btnClose.setOnClickListener(v -> finishAffinity());
    }

    private void copyToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Crash Log", crashLog);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(this, R.string.crash_copied, Toast.LENGTH_SHORT).show();
    }

    private void openWhatsApp() {
        try {
            String phone = getString(R.string.dev_whatsapp_number);
            String text = getString(R.string.whatsapp_message_template, crashLog);
            String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + Uri.encode(text);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.whatsapp_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}