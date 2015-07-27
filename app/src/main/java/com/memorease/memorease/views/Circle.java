package com.memorease.memorease.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.memorease.memorease.R;

public class Circle extends View {

    private static final int START_ANGLE_POINT = 90;

    private final Paint paint;
    private final RectF rect;

    private float angle;

    public Circle(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        final float strokeWidth = dpToPx(1);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        //Circle color
        paint.setColor(getResources().getColor(R.color.button_text));

        rect = new RectF(strokeWidth, strokeWidth, dpToPx(50)-strokeWidth, dpToPx(50)-strokeWidth);

        //Initial Angle (optional, it can be zero)
        angle = 0;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(rect, START_ANGLE_POINT, angle, false, paint);
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(final float angle) {
        this.angle = angle;
    }

    private float dpToPx(final int dp) {
        final Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }
}