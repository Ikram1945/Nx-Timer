package com.nx.timer

import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

object ClickAnimator {

    @JvmStatic
    fun applyToAll(root: View?) {
        if (root == null) return

        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                applyToAll(root.getChildAt(i))
            }
        }

        val isButtonLike = root.isClickable
                || root is MaterialButton
                || root is FloatingActionButton
                || root is android.widget.Button

        if (isButtonLike && root.visibility == View.VISIBLE) {
            if (root.foreground == null && root !is ViewGroup) {
                if (root !is MaterialButton && root !is FloatingActionButton) {
                    try {
                        val outValue = android.util.TypedValue()
                        root.context.theme.resolveAttribute(
                            android.R.attr.selectableItemBackgroundBorderless,
                            outValue,
                            true
                        )
                        root.foreground = root.context.getDrawable(outValue.resourceId)
                    } catch (_: Exception) {
                    }
                }
            }
            addBounceAnim(root)
        }
    }

    private fun addBounceAnim(view: View) {
        if (view.getTag(R.id.tag_anim_applied) != null) return
        view.setTag(R.id.tag_anim_applied, true)

        view.pivotX = view.width / 2f
        view.pivotY = view.height / 2f
        view.addOnLayoutChangeListener(BounceLayoutListener())
        view.setOnTouchListener(BounceTouchListener())
    }
}