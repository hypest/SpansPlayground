package org.hypest.spansplayground;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BulletSpan;
import android.util.Log;
import android.widget.EditText;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

    EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText) findViewById(R.id.edittext);

        mEditText.addTextChangedListener(mTextWatcher);

        mEditText.getText().setSpan(new BulletSpan(), 0, mEditText.getText().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

    TextWatcher mTextWatcher = new TextWatcher() {
        private String spaces(int count) {
            return TextUtils.join("", Collections.nCopies(count, " "));
        }

        private String logSpans(CharSequence chars) {
            Object[] spans = mEditText.getText().getSpans(0, 9999999, Object.class);
            List<Object> spansList = Arrays.asList(spans);

            // sort the spans list by start position and size
            Collections.sort(spansList, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    int diff = mEditText.getText().getSpanStart(o1) - mEditText.getText().getSpanStart(o2);
                    if (diff == 0) {
                        diff = mEditText.getText().getSpanEnd(o1) - mEditText.getText().getSpanEnd(o2);
                    }

                    return diff / Math.abs(diff == 0 ? 1 : diff);
                }
            });

            StringBuilder sb = new StringBuilder();
            sb.append('\n').append(chars.toString().replace('\n', ' '));

            for (Object span : spansList) {
                int start = mEditText.getText().getSpanStart(span);
                int end = mEditText.getText().getSpanEnd(span);

                int gap = chars.length() + 5;

                sb.append('\n');

                if (start > 0) {
                    sb.append(spaces(start));
                    gap -= start;
                }

                sb.append('|');
                gap--;

                if (end - start - 1 > 0) {
                    sb.append(spaces(end - start - 1));
                    gap -= end-start - 1;
                }

                if (end - start > 0) {
                    sb.append('|');
                    gap--;
                }

                sb.append(spaces(gap));

                sb.append("   ")
                        .append(String.format("%03d", start))
                        .append(" -> ")
                        .append(String.format("%03d", end))
                        .append(" : ")
                        .append(span.getClass().getSimpleName());
            }

            return sb.toString();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.v("edit", "beforeTextChanged size=" + s.length() + ", s=" + s + ", start=" + start + ", count=" +
                    count + ", after=" + after + "\n\nspans:" + logSpans(s) + "\n\n ");
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.v("edit", "onTextChanged size=" + s.length() + ", s=" + s + ", start=" + start + ", before=" + before
                    + ", count=" + count + "\n\nspans: " + logSpans(s) + "\n\n ");
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.v("edit", "afterTextChanged size=" + s.length() + ", s=" + s + "\n\nspans:" + logSpans(s) + "\n\n ");
        }
    };
}
