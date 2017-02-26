package org.hypest.spansplayground;

import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.widget.TextView;

class ParagraphCollapseAdjuster implements TextWatcher {
    static void install(TextView text) {
        text.addTextChangedListener(new ParagraphCollapseAdjuster());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int before, int after) {
        if (start + before < s.length()) {
            // change will not reach the end-of-text so, nothing to worry about. Bail.
            return;
        }

        if (before == 0) {
            // change will not remove any text so, no start of an inadvertent paragraph collapse. Bail.
            return;
        }

        // OK, the change will cause an end-of-text paragraph collapse so, mark the paragraphs
        //  with their current anchor position

        for (SpanWrapper<ParagraphFlagged> p :
                SpanWrapper.getSpans((Spannable) s, start, start, ParagraphFlagged.class)) {
            if (p.getStart() == start && p.getEnd() > start) {
                p.getSpan().setStartBeforeCollapse(start);
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int after) {
        if (after == 0) {
            // no addition of characters so, any collapse cannot be reverted yet. We'll wait for when chars are added.
            return;
        }

        // OK, chars where added so, let's check for collapses and adjust
        for (SpanWrapper<ParagraphFlagged> p :
                SpanWrapper.getSpans((Spannable) s, s.length(), s.length(), ParagraphFlagged.class)) {
            if (p.getSpan().hasCollapsed()) {
                p.setStart(p.getSpan().getStartBeforeCollapse());
                p.getSpan().clearStartBeforeCollapse();
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {}
}
