package org.hypest.spansplayground;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.TypefaceSpan;

class SpansHelper {
//    private static class IgnoreDeletion {}

    private static TypefaceSpan newList() {
        return new TypefaceSpan("serif");
    }

    private static BulletSpan newListItem() {
        return new BulletSpan();
    }

    static void newList(Spannable text, int start, int end) {
        setList(text, newList(), start, end);
    }

    static void newListItem(Spannable text, int start, int end) {
        setListItem(text, newListItem(), start, end);
    }

    static void setList(Spannable text, TypefaceSpan list, int start, int end) {
        text.setSpan(list, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    static void setListItem(Spannable text, BulletSpan listItem, int start, int end) {
        text.setSpan(listItem, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

//    static void insertZwj(Editable text, int position) {
//        text.insert(position, "" + Constants.ZWJ_CHAR);
//    }
//
//    static void deleteAndIgnore(Editable text, int start, int count) {
//        text.setSpan(new IgnoreDeletion(), start, start + count, Spanned.SPAN_COMPOSING);
//        text.delete(start, start + count);
//    }
//
//    static boolean handledDeletionIgnore(Spannable text, Spanned textFragment) {
//        IgnoreDeletion[] allowDeletions = textFragment.getSpans(0, 0, IgnoreDeletion.class);
//        if (allowDeletions != null && allowDeletions.length > 0) {
//            // remove the marking span, it's job is finished.
//            text.removeSpan(allowDeletions[0]);
//            return true;
//        }
//
//        return false;
//    }

}
