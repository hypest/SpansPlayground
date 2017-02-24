package org.hypest.spansplayground;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BulletSpan;
import android.text.style.TypefaceSpan;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

class EndOfBufferMarkerAdder implements TextWatcher {
    private int inputStart;
    private Spanned charsNew;

    private final EndOfBufferMarkingInProgress END_OF_BUFFER_MARKING_IN_PROGRESS = new EndOfBufferMarkingInProgress();

    static void install(TextView text) {
        text.addTextChangedListener(new EndOfBufferMarkerAdder());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        inputStart = start;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        charsNew = (Spanned) s.subSequence(start, start + count);

        if (s.charAt(s.length() - 1) == Constants.NEWLINE) {
            ((Spannable) s).setSpan(END_OF_BUFFER_MARKING_IN_PROGRESS, s.length() - 1, s.length(), Spanned.SPAN_INTERMEDIATE);
        }
    }

    static class EndOfBufferMarkingInProgress {}
    static class Replayed {}

    @Override
    public void afterTextChanged(Editable text) {
        // NOTE: According to the documentation, by the time this afterTextChanged have been called, the text might
        //  have been changed by other TextWatcher's afterTextChanged calls. This might introduce some inconsistency
        //  and "random" bugs.

        if (text.charAt(text.length() - 1) == Constants.NEWLINE) {
            int inputStartSaved = inputStart;
            Spannable charsNewSave = (Spannable) charsNew.subSequence(0, charsNew.length());

            Spannable endOfBufferMarker = new SpannableStringBuilder("" + Constants.END_OF_BUFFER_MARKER);
            EndOfBufferMarkingInProgress endOfBufferMarkingInProgress = new EndOfBufferMarkingInProgress();
            endOfBufferMarker.setSpan(endOfBufferMarkingInProgress, 0, endOfBufferMarker.length(), Spanned.SPAN_INTERMEDIATE);

            // append the end-of-text market. This will cause a call to all text listeners.
            text.append(endOfBufferMarker);
            // OK, the text change listeners have now finished. Let's clean up.
            text.removeSpan(endOfBufferMarkingInProgress);
            text.removeSpan(END_OF_BUFFER_MARKING_IN_PROGRESS);

            // replace the newline to cause a text change event. The newline now has updated span statuses.
            Spanned updatedNewline = (Spanned) text.subSequence(text.length() - 2, text.length() - 1);
            Replayed replayed = new Replayed();
            charsNewSave.setSpan(replayed, 0, charsNewSave.length(), Spanned.SPAN_INTERMEDIATE);
            text.replace(inputStartSaved, inputStartSaved + charsNewSave.length(), charsNewSave);
            text.removeSpan(replayed);
        }
    }

    static boolean isEndOfBufferMarkerInProgress(Spanned text) {
        EndOfBufferMarkingInProgress[] endOfBufferMarkingInProgresses = text.getSpans(0, text.length(), EndOfBufferMarkingInProgress.class);
        return  endOfBufferMarkingInProgresses != null && endOfBufferMarkingInProgresses.length > 0;
    }
}
