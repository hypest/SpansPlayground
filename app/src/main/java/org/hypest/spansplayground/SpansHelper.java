package org.hypest.spansplayground;

import android.text.Spannable;
import android.text.Spanned;
import android.text.style.TypefaceSpan;

class SpansHelper {
    static void newList(Spannable text, int start, int end) {
        newList(text, start, end, Spanned.SPAN_PARAGRAPH);
    }

    static void newList(Spannable text, int start, int end, int flags) {
        setList(text, new ListSpan(), start, end, flags);
    }

    static void newListItem(Spannable text, int start, int end) {
        newListItem(text, start, end, Spanned.SPAN_PARAGRAPH);
    }

    static void newListItem(Spannable text, int start, int end, int flags) {
        setListItem(text, new ListItemSpan(), start, end, flags);
    }

    static void setList(Spannable text, TypefaceSpan list, int start, int end) {
        setList(text, list, start, end, text.getSpanFlags(list));
    }

    private static void setList(Spannable text, TypefaceSpan list, int start, int end, int flags) {
        text.setSpan(list, start, end, flags);
    }

    static void setListItem(Spannable text, android.text.style.BulletSpan listItem, int start, int end) {
        setListItem(text, listItem, start, end, text.getSpanFlags(listItem));
    }

    private static void setListItem(Spannable text, android.text.style.BulletSpan listItem, int start, int end, int flags) {
        text.setSpan(listItem, start, end, flags);
    }
}
