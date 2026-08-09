package com.nx.timer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple bar chart untuk visualisasi durasi harian/mingguan.
 */
public class DurationBarChart extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<BarData> data = new ArrayList<>();
    private float maxValue = 1f;

    public DurationBarChart(Context context) {
        super(context);
        init();
    }

    public DurationBarChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint.setColor(0xFF6366F1); // Indigo modern yang elegan
        barPaint.setStyle(Paint.Style.FILL);
        labelPaint.setColor(0xFF555555);
        labelPaint.setTextSize(26f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<BarData> newData) {
        data.clear();
        data.addAll(newData);

        maxValue = 0f;
        for (BarData d : data) {
            if (d.value > maxValue) maxValue = d.value;
        }
        if (maxValue <= 0) maxValue = 1f; // hindari pembagian nol

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data.isEmpty()) return;

        int w = getWidth();
        int h = getHeight();
        int padding = 28;
        int count = data.size();
        float availableWidth = w - padding * 2f;
        float barWidth = availableWidth / (count * 1.7f);
        float spacing = barWidth * 0.7f;
        float cornerRadius = 12f;

        int[] colors = {0xFF6366F1, 0xFF818CF8}; // gradient indigo

        for (int i = 0; i < count; i++) {
            BarData d = data.get(i);
            float ratio = d.value / maxValue;
            float barHeight = ratio * (h - 95);

            float left = padding + i * (barWidth + spacing);
            float top = h - 50 - barHeight;
            float right = left + barWidth;
            float bottom = h - 50;

            // Buat gradient vertikal
            LinearGradient gradient = new LinearGradient(
                    left, top, left, bottom,
                    colors[0], colors[1], Shader.TileMode.CLAMP);
            barPaint.setShader(gradient);

            RectF rect = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaint);

            canvas.drawText(d.label, left + barWidth / 2f, h - 16, labelPaint);
        }
    }

    public static class BarData {
        public final String label;
        public final float value;

        public BarData(String label, float value) {
            this.label = label;
            this.value = value;
        }
    }
}