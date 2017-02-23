package org.hypest.spansplayground;

import android.text.Spanned;

class TextChangedEvent {
    final int inputStart;
    final Spanned charsOld;
    final Spanned charsNew;
    final boolean gotNewline;
    final boolean deletedNewline;
    final int newlineIndex;

    private boolean doSetSelection = false;
    private int selectionPosition;

    TextChangedEvent(Spanned text, int inputStart, Spanned charsOld, Spanned charsNew) {
        this.inputStart = inputStart;
        this.charsOld = charsOld;
        this.charsNew = charsNew;

        gotNewline =
                charsNew.length() == 1
                        && charsNew.charAt(0) == Constants.NEWLINE;

        deletedNewline =
                charsOld.length() > 0
                        && charsNew.length() == 0
                        && charsOld.charAt(charsOld.length() - 1) == Constants.NEWLINE; // the framework seems to remove
                                    // more than the newline and then add the text again sans the newline so, check the
                                    // last char to detect the newline

        if (deletedNewline) {
            newlineIndex = inputStart + charsOld.length() - 1;
        } else {
            newlineIndex = inputStart;
        }
    }

    boolean doSetSelection() {
        return doSetSelection;
    }

    int getSelectionPosition() {
        return selectionPosition;
    }

    void postSetSelection(int selectionPosition) {
        doSetSelection = true;
        this.selectionPosition = selectionPosition;
    }
}
