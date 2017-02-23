package org.hypest.spansplayground;

import android.text.Spanned;

class TextChangedEvent {
    final int inputStart;
    final Spanned charsOld;
    final Spanned charsNew;
    final boolean gotNewline;
//    final boolean gotNewlineAfterZwj;
//    final char zwjRightNeighbor;
//    final boolean deletedZwj;
    final boolean deletedNewline;
    final int newlineIndex;

    TextChangedEvent(Spanned text, int inputStart, Spanned charsOld, Spanned charsNew) {
        this.inputStart = inputStart;
        this.charsOld = charsOld;
        this.charsNew = charsNew;

        gotNewline =
                charsNew.length() == 1
                        && charsNew.charAt(0) == Constants.NEWLINE;

//        zwjRightNeighbor =
//                (inputStart > 0
//                        && charsNew.length() > 0
//                        && text.charAt(inputStart - 1) == Constants.ZWJ_CHAR) ? charsNew.charAt(0) : 0;
//
//        gotNewlineAfterZwj = zwjRightNeighbor == Constants.NEWLINE;
//
//        deletedZwj =
//                charsOld.length() == 1
//                        && charsNew.length() == 0
//                        && charsOld.charAt(0) == Constants.ZWJ_CHAR;

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
}
