package org.hypest.spansplayground;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;

class ListItemSpan implements ParagraphFlagged, LeadingMarginSpan {
    private final int mGapWidth;
    private final boolean mWantColor;
    private final int mColor;

    private static final int BULLET_RADIUS = 3;
    private static Path sBulletPath = null;
    static final int STANDARD_GAP_WIDTH = 2;

    private int mStartBeforeColapse = -1;
    private int mEndBeforeBleed = -1;

    ListItemSpan() {
        mGapWidth = STANDARD_GAP_WIDTH;
        mWantColor = false;
        mColor = 0;
    }

    @Override
    public int getLeadingMargin(boolean first) {
        return 2 * BULLET_RADIUS + mGapWidth;
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
            int top, int baseline, int bottom,
            CharSequence text, int start, int end,
            boolean first, Layout l) {
        if (((Spanned) text).getSpanStart(this) == start) {
            Paint.Style style = p.getStyle();
            int oldcolor = 0;

            if (mWantColor) {
                oldcolor = p.getColor();
                p.setColor(mColor);
            }

            p.setStyle(Paint.Style.FILL);

            if (c.isHardwareAccelerated()) {
                if (sBulletPath == null) {
                    sBulletPath = new Path();
                    // Bullet is slightly better to avoid aliasing artifacts on mdpi devices.
                    sBulletPath.addCircle(0.0f, 0.0f, 1.2f * BULLET_RADIUS, Path.Direction.CW);
                }

                c.save();
                c.translate(x + dir * BULLET_RADIUS, (top + bottom) / 2.0f);
                c.drawPath(sBulletPath, p);
                c.restore();
            } else {
                c.drawCircle(x + dir * BULLET_RADIUS, (top + bottom) / 2.0f, BULLET_RADIUS, p);
            }

            if (mWantColor) {
                p.setColor(oldcolor);
            }

            p.setStyle(style);
        }
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
