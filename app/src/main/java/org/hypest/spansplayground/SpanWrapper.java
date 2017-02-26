package org.hypest.spansplayground;

import android.text.Spannable;

import java.lang.ref.WeakReference;

class SpanWrapper<T> {
    private WeakReference<Spannable> mSpannable;
    private WeakReference<T> mSpan;

    static <T> SpanWrapper<T>[] getSpans(Spannable spannable, int start, int end, Class<T> type) {
        T[] spanObjects = spannable.getSpans(start, end, type);
        SpanWrapper<T>[] spanWrappers = new SpanWrapper[spanObjects.length];
        for (int i = 0; i < spanObjects.length; i++) {
            spanWrappers[i] = new SpanWrapper<>(spannable, spanObjects[i]);
        }

        return spanWrappers;
    }

    static <T> SpanWrapper<T>[] getSpans(Spannable spannable, T[] spanObjects) {
        SpanWrapper<T>[] spanWrappers = new SpanWrapper[spanObjects.length];
        for (int i = 0; i < spanObjects.length; i++) {
            spanWrappers[i] = new SpanWrapper<>(spannable, spanObjects[i]);
        }

        return spanWrappers;
    }

    SpanWrapper(Spannable spannable, T span) {
        mSpannable = new WeakReference<>(spannable);
        mSpan = new WeakReference<>(span);
    }

    T getSpan() {
        return mSpan.get();
    }

    void remove() {
        if (mSpannable.get() == null || mSpan.get() == null) {
            return;
        }

        mSpannable.get().removeSpan(mSpan.get());
    }

    int getStart() {
        if (mSpannable.get() == null || mSpan.get() == null) {
            return -1;
        }

        return mSpannable.get().getSpanStart(mSpan.get());
    }

    int getEnd() {
        if (mSpannable.get() == null || mSpan.get() == null) {
            return -1;
        }

        return mSpannable.get().getSpanEnd(mSpan.get());
    }

    int getFlags() {
        if (mSpannable.get() == null || mSpan.get() == null) {
            return -1;
        }

        return mSpannable.get().getSpanFlags(mSpan.get());
    }

    void setStart(int start) {
        if (mSpannable.get() == null || mSpan.get() == null) {
            return;
        }

        mSpannable.get().setSpan(mSpan.get(), start, getEnd(), getFlags());
    }

    void setEnd(int end) {
        if (mSpannable.get() == null || mSpan.get() == null) {
            return;
        }

        mSpannable.get().setSpan(mSpan.get(), getStart(), end, getFlags());
    }

    void setFlags(int flags) {
        if (mSpannable.get() == null || mSpan.get() == null) {
            return;
        }

        mSpannable.get().setSpan(mSpan.get(), getStart(), getEnd(), flags);
    }
}
