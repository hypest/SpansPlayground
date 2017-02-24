package org.hypest.spansplayground;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.TypefaceSpan;

class SpansHelper {
//    enum Zipping {
//        MARK_NORMAL,
//        MARK_ZIPPED,
//    }

    private static class Ignore {}

    private static TypefaceSpan newList() {
        return new TypefaceSpan("serif");
    }

    private static BulletSpan newListItem() {
        return new BulletSpan();
    }

    static void newList(Spannable text, int start, int end) {
        newList(text, start, end, Spanned.SPAN_PARAGRAPH);
    }

    static void newList(Spannable text, int start, int end, int flags) {
        setList(text, newList(), start, end, flags);
    }

    static void newListItem(Spannable text, int start, int end) {
        newListItem(text, start, end, Spanned.SPAN_PARAGRAPH);
//        newListItem(text, start, end, flagUnzip(Spanned.SPAN_PARAGRAPH));
    }

    static void newListItem(Spannable text, int start, int end, int flags) {
        setListItem(text, newListItem(), start, end, flags);
//        setListItem(text, newListItem(), start, end, flagUnzip(flags));
    }

    static void setList(Spannable text, TypefaceSpan list, int start, int end) {
        setList(text, list, start, end, text.getSpanFlags(list));
    }

    private static void setList(Spannable text, TypefaceSpan list, int start, int end, int flags) {
        text.setSpan(list, start, end, flags);
    }

    static void setListItem(Spannable text, BulletSpan listItem, int start, int end) {
        setListItem(text, listItem, start, end, text.getSpanFlags(listItem));
    }

    private static void setListItem(Spannable text, BulletSpan listItem, int start, int end, int flags) {
        text.setSpan(listItem, start, end, flags);
    }

    static void openListEnd(Spannable text, TypefaceSpan list) {
//        text.setSpan(list, text.getSpanStart(list), text.getSpanEnd(list), FLAG_OPEN_ENDED);
    }

    static void closeListEnd(Spannable text, TypefaceSpan list) {
//        text.setSpan(list, text.getSpanStart(list), text.getSpanEnd(list), FLAG_CLOSE_ENDED);
    }

    static void openListItemEnd(Spannable text, BulletSpan listItem) {
//        text.setSpan(listItem, text.getSpanStart(listItem), text.getSpanEnd(listItem), FLAG_OPEN_ENDED);
    }

    static void closeListItemEnd(Spannable text, BulletSpan listItem) {
//        text.setSpan(listItem, text.getSpanStart(listItem), text.getSpanEnd(listItem), FLAG_CLOSE_ENDED);
    }

    static void deleteAndIgnore(Editable text, int start, int count) {
        text.setSpan(new Ignore(), start, start + count, Spanned.SPAN_COMPOSING);
        text.delete(start, start + count);
    }

    static void appendAndIgnore(Editable text, CharSequence chars) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(chars);
        ssb.setSpan(new Ignore(), 0, ssb.length(), Spanned.SPAN_COMPOSING);

        text.append(ssb);
    }

    static boolean handledIgnore(Spannable text, Spanned textFragment) {
        Ignore[] ignores = textFragment.getSpans(0, 0, Ignore.class);
        if (ignores != null && ignores.length > 0) {
            // remove the marking span, it's job is finished.
            text.removeSpan(ignores[0]);
            return true;
        }

        return false;
    }

    static boolean hasList(Spanned text, int start, int end) {
        TypefaceSpan[] lists = text.getSpans(start, end, TypefaceSpan.class);
        return lists != null && lists.length > 0;
    }

//    static int flagUnzip(int flags) {
//        return (flags & ~Spanned.SPAN_USER) | (Zipping.MARK_ZIPPED.ordinal() << Spanned.SPAN_USER_SHIFT);
//    }
//
//    static boolean isFlaggedUnzip(int flags) {
//        return Zipping.MARK_ZIPPED.ordinal() == ((flags & Spanned.SPAN_USER) >>> Spanned.SPAN_USER_SHIFT);
//    }
//
//    static void flagUnzip(Spannable text, Object span) {
//        text.setSpan(span, text.getSpanStart(span), text.getSpanEnd(span), flagUnzip(text.getSpanFlags(span)));
//    }
//
//    static boolean isFlaggedUnzip(Spanned text, Object span) {
//        return isFlaggedUnzip(text.getSpanFlags(span));
//    }
}
