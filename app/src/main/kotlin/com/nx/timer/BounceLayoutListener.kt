package com.nx.timer

import android.view.View

class BounceLayoutListener : View.OnLayoutChangeListener {
    override fun onLayoutChange(
        v: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        v.pivotX = v.width / 2f
        v.pivotY = v.height / 2f
    }
}