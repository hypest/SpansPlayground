package org.hypest.spansplayground;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.style.BulletSpan;

class ListItemSpan extends BulletSpan implements ParagraphFlagged {
    private int mStartBeforeColapse = -1;
    private int mEndBeforeBleed = -1;

    ListItemSpan() {
        super();
    }

    ListItemSpan(Parcel src) {
        super(src);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Parcelable.Creator<ListItemSpan> CREATOR = new Parcelable.Creator<ListItemSpan>() {
        @Override
        public ListItemSpan createFromParcel(Parcel in) {
            return new ListItemSpan(in);
        }

        @Override
        public ListItemSpan[] newArray(int size) {
            return new ListItemSpan[size];
        }
    };

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
