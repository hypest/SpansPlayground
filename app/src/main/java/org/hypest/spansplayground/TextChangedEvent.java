package org.hypest.spansplayground;

import android.text.Spanned;

class TextChangedEvent {
    final int inputStart;
    final Spanned charsNew;
    final boolean gotNewline;
    final boolean gotEndOfBufferMarker;

    TextChangedEvent(int inputStart, Spanned charsNew) {
        this.inputStart = inputStart;
        this.charsNew = charsNew;

        gotNewline = (charsNew.length() == 1 && charsNew.charAt(0) == Constants.NEWLINE)
                || (charsNew.length() == 2 && charsNew.charAt(0) == Constants.NEWLINE
                        && charsNew.charAt(1) == Constants.END_OF_BUFFER_MARKER);

        gotEndOfBufferMarker = (charsNew.length() == 1 && charsNew.charAt(0) == Constants.END_OF_BUFFER_MARKER);
    }
}
