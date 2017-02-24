package org.hypest.spansplayground;

import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.TypefaceSpan;

class ListGarbageCollection implements SpanWatcher {
    @Override
    public void onSpanAdded(Spannable text, Object what, int start, int end) {
        // nothing to do here
    }

    @Override
    public void onSpanRemoved(Spannable text, Object what, int start, int end) {
        // nothing to do here
    }

    @Override
    public void onSpanChanged(Spannable text, Object what, int ostart, int oend, int nstart, int nend) {
        if (nstart != nend) {
            // only care about removing empty spans
            return;
        }

        if (!(what instanceof BulletSpan || what instanceof TypefaceSpan)) {
            // only care about removing List related spans
            return;
        }

//        if (SpansHelper.isFlaggedUnzip(text, what)) {
//            // spans marked as zipped need to be let alone to be unzipped so, let them live
//            return;
//        }

        // semove the span. This basically cleans up any list or list items that their line(s) got completely deleted.
        text.removeSpan(what);
    }

    static void install(Spannable text) {
        text.setSpan(new ListGarbageCollection(), 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }
}
