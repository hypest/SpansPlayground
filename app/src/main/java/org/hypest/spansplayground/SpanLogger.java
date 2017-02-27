package org.hypest.spansplayground;

import android.text.Spanned;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by hypest on 11/01/17.
 */
class SpanLogger {
    public static SpanLogger INSTANCE = new SpanLogger();

    private static String spaces(int count, String c) {
        return TextUtils.join("", Collections.nCopies(count, c));
    }

    private static String logSpans(Spanned text){
        Object[] spans = text.getSpans(0, 9999999, Object.class);
        List<Object> spansList = Arrays.asList(spans);

        StringBuilder sb = new StringBuilder();
        sb.append('\n').append(text.toString().replace('\n', '¶').replace('\u200B', '¬')); // ␤↵↭
        sb.append("  length = " + text.length());

        for (Object span : spansList) {
            int start = text.getSpanStart(span);
            int end = text.getSpanEnd(span);

            int gap = text.length() + 5;

            sb.append('\n');

            if (start > 0) {
                sb.append(spaces(start, " "));
                gap -= start;
            }

            int spanMode = text.getSpanFlags(span) & Spanned.SPAN_POINT_MARK_MASK;

            if (end - start > 0) {
                sb.append((spanMode == Spanned.SPAN_EXCLUSIVE_INCLUSIVE || spanMode == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) ? '<' : '>');
                gap--;
            } else {
                if (spanMode == Spanned.SPAN_INCLUSIVE_INCLUSIVE) {
                    sb.append('x');
                } else if (spanMode == Spanned.SPAN_INCLUSIVE_EXCLUSIVE) {
                    sb.append('>');
                } else if (spanMode == Spanned.SPAN_EXCLUSIVE_INCLUSIVE) {
                    sb.append('<');
                } else if (spanMode == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) {
                    sb.append('o');
                }
            }

            if (end - start - 1 > 0) {
                sb.append(spaces(end - start - 1, "-"));
                gap -= end - start - 1;
            }

            if (end - start > 0) {
                sb.append((spanMode == Spanned.SPAN_INCLUSIVE_EXCLUSIVE || spanMode == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) ? '>' : '<');
                gap--;
            }

            sb.append(spaces(gap, " "));

            sb.append("   ")
                    .append(String.format("%03d", start))
                    .append(" -> ")
                    .append(String.format("%03d", end))
                    .append(" : ")
                    .append(span.getClass().getSimpleName());
        }

        return sb.toString();
    }
}
