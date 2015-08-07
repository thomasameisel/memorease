package com.tarian.memorease.custom_views;

import android.view.animation.Animation;
import android.view.animation.Transformation;

public class CircleAngleAnimation extends Animation {

    private final Circle circle;

    private final float oldAngle;
    private final float newAngle;

    public CircleAngleAnimation(final Circle circle, final int newAngle) {
        this.oldAngle = circle.getAngle();
        this.newAngle = newAngle;
        this.circle = circle;
    }

    @Override
    protected void applyTransformation(final float interpolatedTime, final Transformation transformation) {
        final float angle = oldAngle + ((newAngle - oldAngle) * interpolatedTime);

        circle.setAngle(angle);
        circle.requestLayout();
    }
}
