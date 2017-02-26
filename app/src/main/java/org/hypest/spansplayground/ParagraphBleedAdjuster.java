package org.hypest.spansplayground;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.TextView;

class ParagraphBleedAdjuster implements TextWatcher {
    static void install(TextView text) {
        text.addTextChangedListener(new ParagraphBleedAdjuster());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int before, int after) {
        if (start == 0) {
            // change will be at the beginning of text so, nothing to worry about. Bail.
            return;
        }

        if (before == 0) {
            // change is only adding characters so, will not start an inadvertent paragraph bleed.
            return;
        }

        if (start + before < s.length()) {
            // change will not reach the end-of-text so, nothing to worry about. Bail.
            return;
        }

        if (s.charAt(start - 1) != Constants.NEWLINE) {
            // no newline will touch the end-of-text during the replace so, nothing to worry about. Bail.
            return;
        }

        // OK, the change will cause an end-of-text paragraph bleed so, mark the paragraphs
        //  with their current anchor position

        Spanned newline = (Spanned) s.subSequence(start - 1, start);
        for (SpanWrapper<ParagraphFlagged> p :
                SpanWrapper.getSpans((Spannable) s, newline.getSpans(0, 1, ParagraphFlagged.class))) {
            if (p.getStart() < start && p.getEnd() == start) {
                p.getSpan().setEndBeforeBleed(start);
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int after) {
        if (after == 0) {
            // no addition of characters so, any bleeding cannot bleed. We'll wait for when chars are added. Bail.
            return;
        }

        // OK, chars where added so, let's check for bleeding and adjust
        for (SpanWrapper<ParagraphFlagged> p :
                SpanWrapper.getSpans((Spannable) s, start, start, ParagraphFlagged.class)) {
            if (p.getSpan().hasBled()) {
                p.setEnd(p.getSpan().getEndBeforeBleed());
                p.getSpan().clearEndBeforeBleed();
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {}
}
