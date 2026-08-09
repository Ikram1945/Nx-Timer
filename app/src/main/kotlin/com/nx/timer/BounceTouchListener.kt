package com.nx.timer

import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

class BounceTouchListener : View.OnTouchListener {
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.animate().cancel()
                v.animate()
                    .scaleX(0.92f)
                    .scaleY(0.92f)
                    .alpha(0.92f)
                    .setDuration(90)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            MotionEvent.ACTION_UP -> {
                v.animate().cancel()
                v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(220)
                    .setInterpolator(OvershootInterpolator(4f))
                    .start()
            }
            MotionEvent.ACTION_CANCEL -> {
                v.animate().cancel()
                v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
        }
        // Return false — jangan konsumsi event.
        // Biarkan view menangani kliknya sendiri (OnClickListener tetap jalan normal).
        return false
    }
}