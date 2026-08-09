package com.nx.timer;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;

/**
 * Global uncaught exception handler that shows a crash dialog
 * instead of the default force-close dialog.
 */
public final class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";
    private static final String EXTRA_CRASH_LOG = "crash_log";
    private static final String EXTRA_CRASH_MESSAGE = "crash_message";

    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final WeakReference<Application> appRef;

    public CrashHandler(Application app) {
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.appRef = new WeakReference<>(app);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        String crashLog = getStackTraceString(throwable);
        String crashMessage = getRootMessage(throwable);

        Log.e(TAG, "Uncaught exception", throwable);

        Application app = appRef.get();
        if (app != null) {
            // Kill all existing activities so the crash dialog is the only thing on screen
            app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

                @Override
                public void onActivityStarted(Activity activity) {}

                @Override
                public void onActivityResumed(Activity activity) {}

                @Override
                public void onActivityPaused(Activity activity) {}

                @Override
                public void onActivityStopped(Activity activity) {}

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

                @Override
                public void onActivityDestroyed(Activity activity) {
                    // Finish all activities except the crash dialog
                    if (!(activity instanceof CrashDialogActivity)) {
                        activity.finish();
                    }
                }
            });

            Intent intent = new Intent(app, CrashDialogActivity.class);
            intent.putExtra(EXTRA_CRASH_LOG, crashLog);
            intent.putExtra(EXTRA_CRASH_MESSAGE, crashMessage);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            app.startActivity(intent);
        }

        // Kill the process after a short delay to let the dialog appear
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}
        Process.killProcess(Process.myPid());
        System.exit(10);
    }

    private String getStackTraceString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    private String getRootMessage(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String msg = cause.getMessage();
        if (msg == null || msg.isEmpty()) {
            msg = cause.getClass().getSimpleName();
        }
        return msg;
    }

    /**
     * Install this crash handler globally on the given Application.
     */
    public static void install(Application app) {
        CrashHandler handler = new CrashHandler(app);
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }
}