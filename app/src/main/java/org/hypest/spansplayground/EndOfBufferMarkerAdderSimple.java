package org.hypest.spansplayground;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.TextView;

class EndOfBufferMarkerAdderSimple implements TextWatcher {
    static void install(TextView text) {
        text.addTextChangedListener(new EndOfBufferMarkerAdderSimple());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable text) {
        // NOTE: According to the documentation, by the time this afterTextChanged have been called, the text might
        //  have been changed by other TextWatcher's afterTextChanged calls. This might introduce some inconsistency
        //  and "random" bugs.

        if (text.length() == 0) {
            // well, text is empty. no need for end-of-text marker
            return;
        }

        switch (text.charAt(text.length() - 1)) {
            case Constants.NEWLINE:
                // need to add a ZWJ so a block element can render at the last line.
                text.append("" + Constants.END_OF_BUFFER_MARKER);

                // by the way, the cursor will be adjusted "automatically" by RichTextEditText's onSelectionChanged to
                //  before the marker
                break;

            case Constants.END_OF_BUFFER_MARKER:
                // there's a marker but let's make sure it's still needed.

                if (text.length() < 2) {
                    // it seems that the marker is alone. Let's leave it there so blocks can render.
                    break;
                }

                // there's a marker but let's make sure it's still needed. Remove it if no newline before it.
                if (text.charAt(text.length() - 2) != Constants.NEWLINE) {
                    // dangling end marker. Let's remove it.
                    text.delete(text.length() - 1, text.length());
                }
                break;

            default:
                // there's some char at text-end so, let's just make sure we don't have dangling text-end markers around
                do {
                    int lastZwjIndex = text.toString().lastIndexOf(Constants.ZWJ);

                    if (lastZwjIndex == -1) {
                        break;
                    }

                    text.delete(lastZwjIndex, lastZwjIndex + 1);
                } while (true);
        }
    }
}
