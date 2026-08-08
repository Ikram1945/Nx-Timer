package com.nx.timer;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public final class ClickAnimator {

    private ClickAnimator() {}

    public static void applyToAll(View root) {
        if (root == null) return;

        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyToAll(group.getChildAt(i));
            }
        }

        boolean isButtonLike = root.isClickable()
                || root instanceof MaterialButton
                || root instanceof FloatingActionButton
                || root instanceof android.widget.Button;

        if (isButtonLike && root.getVisibility() == View.VISIBLE) {
            if (root.getForeground() == null && !(root instanceof ViewGroup)) {
                if (!(root instanceof MaterialButton)
                        && !(root instanceof FloatingActionButton)) {
                    try {
                        android.util.TypedValue outValue = new android.util.TypedValue();
                        root.getContext().getTheme().resolveAttribute(
                                android.R.attr.selectableItemBackgroundBorderless, outValue, true);
                        root.setForeground(root.getContext().getDrawable(outValue.resourceId));
                    } catch (Exception ignored) {}
                }
            }
            addBounceAnim(root);
        }
    }

    private static void addBounceAnim(final View view) {
        if (view.getTag(R.id.tag_anim_applied) != null) return;
        view.setTag(R.id.tag_anim_applied, true);

        view.setPivotX(view.getWidth() / 2f);
        view.setPivotY(view.getHeight() / 2f);
        view.addOnLayoutChangeListener(new BounceLayoutListener());
        view.setOnTouchListener(new BounceTouchListener());
    }
}