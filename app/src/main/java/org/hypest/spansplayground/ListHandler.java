package org.hypest.spansplayground;

import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.TextView;

class ListHandler implements TextWatcher {
    private int inputStart;
    private Spanned charsNew;

    static void install(TextView text) {
        text.addTextChangedListener(new ListHandler());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        inputStart = start;
        charsNew = (Spanned) s.subSequence(start, start + count);
    }

    @Override
    public void afterTextChanged(Editable text) {
        // NOTE: According to the documentation, by the time this afterTextChanged have been called, the text might
        //  have been changed by other TextWatcher's afterTextChanged calls. This might introduce some inconsistency
        //  and "random" bugs.

        final TextChangedEvent textChangedEvent = new TextChangedEvent(inputStart, charsNew);
        ListHelper.handleTextChangeForLists(text, textChangedEvent);
    }
}
