package org.hypest.spansplayground;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.util.AttributeSet;
import android.widget.EditText;

class RichTextEditText extends EditText {
    public RichTextEditText(Context context) {
        super(context);
    }

    public RichTextEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RichTextEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @CallSuper
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);

        if (length() == 0) {
            // no text at all so, no need to cater for cursor positioning relative to a end-of-text marker.
            return;
        }

        // if the text end has the marker, let's make sure the cursor never includes it or surpusses it
        if ((selStart == length() || selEnd == length())
                && getText().charAt(length() - 1) == Constants.END_OF_BUFFER_MARKER) {
            int start = selStart;
            int end = selEnd;

            if (start == length()) {
                start--;
            }

            if (end == length()) {
                end--;
            }

            setSelection(start, end);
        }
    }
}
