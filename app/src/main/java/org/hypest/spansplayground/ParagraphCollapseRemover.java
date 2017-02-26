package org.hypest.spansplayground;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.TextView;

class ParagraphCollapseRemover implements TextWatcher {
    static void install(TextView text) {
        text.addTextChangedListener(new ParagraphCollapseRemover());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        int end = start + count;

        Spanned charsOld = (Spanned) s.subSequence(start, start + count);

        SpanWrapper<ParagraphFlagged>[] paragraphs = SpanWrapper.getSpans((Spannable) s,
                start, start + count, ParagraphFlagged.class);

        if (paragraphs.length == 0) {
            // no paragraphs in the text to be removed so, nothing to do here. Bail.

            if (start + count < s.length()) {
                // try adjacent paragraphs as well
                paragraphs = SpanWrapper.getSpans((Spannable) s, start, start + count + 1, ParagraphFlagged.class);
                end = start + count + 1;
            } else {
                return;
            }
        }

        int firstNewlineBeyondChangeIndex = s.toString().indexOf(Constants.NEWLINE, start + count);
        if (firstNewlineBeyondChangeIndex == -1) {
            // no newline beyond the change so, let's set it to the text end.
            firstNewlineBeyondChangeIndex = s.length();
        }

        String charsOldString = charsOld.toString();

        int lastNewlineIndex = charsOldString.length();
        do {
            lastNewlineIndex = charsOldString.lastIndexOf(Constants.NEWLINE, lastNewlineIndex - 1);

            if (lastNewlineIndex == -1) {
                break;
            }

            if (start + lastNewlineIndex + 2 > s.length()) {

                continue;
            }

            ParagraphFlagged[] paragraphsToCheck;

            if (start + lastNewlineIndex + 1 < s.length()) {
                Spanned postNewline = (Spanned) s.subSequence(start + lastNewlineIndex + 1, start + lastNewlineIndex + 2);
                paragraphsToCheck = postNewline.getSpans(0, 1, ParagraphFlagged.class);
            } else {
                paragraphsToCheck = charsOld.getSpans(lastNewlineIndex + 1, lastNewlineIndex + 1, ParagraphFlagged.class);
            }

            for (SpanWrapper<ParagraphFlagged> p : SpanWrapper.getSpans((Spannable) s, paragraphsToCheck)) {
                if (p.getStart() == start + lastNewlineIndex + 1) {
                    // this paragraph is anchored to the newline in question

                    if (p.getEnd() > firstNewlineBeyondChangeIndex + 1) {
                        // paragraph end is beyond the newline that will be picked up. That means the paragraph
                        //  will manage to get to the new anchor without totally collapsing. Let's move on.
                        continue;
                    }

                    // paragraph end is closer or at the newline that will be picked up. That means the paragraph
                    //  will effectively collapse since its start will reach its end before the newline. Retire it.
                    p.remove();
                }
            }
        } while (lastNewlineIndex > -1);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {}
}
