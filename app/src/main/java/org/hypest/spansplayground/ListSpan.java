package org.hypest.spansplayground;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

class ListSpan extends MetricAffectingSpan implements ParagraphFlagged {
    private final String mFamily;

    private int mStartBeforeColapse = -1;
    private int mEndBeforeBleed = -1;

    ListSpan() {
        this("serif");
    }

    ListSpan(String family) {
        mFamily = family;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        apply(ds, mFamily);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        apply(paint, mFamily);
    }

    private static void apply(Paint paint, String family) {
        int oldStyle;

        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }

        Typeface tf = Typeface.create(family, oldStyle);
        int fake = oldStyle & ~tf.getStyle();

        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTypeface(tf);
    }

    @Override
    public int getStartBeforeCollapse() {
        return mStartBeforeColapse;
    }

    @Override
    public void setStartBeforeCollapse(int startBeforeCollapse) {
        mStartBeforeColapse = startBeforeCollapse;
    }

    @Override
    public void clearStartBeforeCollapse() {
        mStartBeforeColapse = -1;
    }

    @Override
    public boolean hasCollapsed() {
        return mStartBeforeColapse != -1;
    }

    @Override
    public int getEndBeforeBleed() {
        return mEndBeforeBleed;
    }

    @Override
    public void setEndBeforeBleed(int endBeforeBleed) {
        mEndBeforeBleed = endBeforeBleed;
    }

    @Override
    public void clearEndBeforeBleed() {
        mEndBeforeBleed = -1;
    }

    @Override
    public boolean hasBled() {
        return mEndBeforeBleed > -1;
    }
}
