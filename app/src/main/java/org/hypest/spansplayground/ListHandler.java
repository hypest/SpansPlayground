package org.hypest.spansplayground;

import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BulletSpan;
import android.text.style.TypefaceSpan;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

class ListHandler implements TextWatcher {
    private int inputStart;
    private Spanned charsOld;
    private Spanned charsNew;

    static void install(TextView text) {
        text.addTextChangedListener(new ListHandler());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        inputStart = start;
        charsOld = (Spanned) s.subSequence(start, start + count);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        charsNew = (Spanned) s.subSequence(start, start + count);
    }

    @Override
    public void afterTextChanged(Editable text) {
        // NOTE: According to the documentation, by the time this afterTextChanged have been called, the text might
        //  have been changed by other TextWatcher's afterTextChanged calls. This might introduce some inconsistency
        //  and "random" bugs.

        if (!EndOfBufferMarkerAdder.isEndOfBufferMarkerInProgress(charsNew)) {
            final TextChangedEvent textChangedEvent = new TextChangedEvent(text, inputStart, charsOld, charsNew);
            ListHelper.handleTextChangeForLists(text, textChangedEvent);

            ensureEndTextMarker(text, charsOld, charsNew);
        } else {
            pushback(text, null, charsNew);
        }
    }

    private void ensureEndTextMarker(Editable text, Spanned charsOld, Spanned charsNew) {
        switch (text.charAt(text.length() - 1)) {
            case Constants.NEWLINE:
                // need to add a ZWJ so a block element can render at the last line.
                text.append("" + Constants.END_OF_BUFFER_MARKER);

                pushback(text, charsOld, new SpannableStringBuilder(charsNew).append(Constants.END_OF_BUFFER_MARKER));

                // by the way, the cursor will be adjusted "automatically" by RichTextEditText's onSelectionChanged to
                //  before the marker
                break;

            case Constants.END_OF_BUFFER_MARKER:
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

                pushback(text, charsOld, charsNew);
        }
    }

    private void pushback(Editable text, Spanned charsOld, Spanned charsNew) {
        int pushbackCount = charsNew.length();

        // let's push back the paragraphs that were not ending at the end before
        List<Object> originals = charsOld != null ? Arrays.asList(charsOld.getSpans(0, 1, Object.class)) : null;

        Spanned currentSpannedAtEnd = (Spanned) text.subSequence(text.length() - 1, text.length());

        TypefaceSpan[] lists = currentSpannedAtEnd.getSpans(0, 1, TypefaceSpan.class);
        for (TypefaceSpan list : lists) {
            if (originals != null && originals.contains(list)) {
                // expand the list that got squashed!
                if (text.getSpanStart(list) == text.getSpanEnd(list)) {
                    SpansHelper.setList(text, list, text.length() - pushbackCount, text.length());
                }
            } else {
                // push back up to the list since it was not at the end-of-text before
                if (text.getSpanStart(list) != text.getSpanEnd(list)) {
                    SpansHelper.setList(text, list, text.getSpanStart(list), text.length() - pushbackCount);
                }
            }
        }

        BulletSpan[] listItems = currentSpannedAtEnd.getSpans(0, 1, BulletSpan.class);
        for (BulletSpan listItem : listItems) {
            if (originals != null && originals.contains(listItem)) {
                // expand the bullet that got squashed!
                if (text.getSpanStart(listItem) == text.getSpanEnd(listItem)) {
                    SpansHelper.setListItem(text, listItem, text.length() - pushbackCount, text.length());
                }
            } else {
                // push back up to the bullet since it was not at the end-of-text before
                if (text.getSpanStart(listItem) != text.getSpanEnd(listItem)) {
                    SpansHelper.setListItem(text, listItem, text.getSpanStart(listItem), text.length() - pushbackCount);
                }
            }
        }
    }
}
