package org.hypest.spansplayground;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BulletSpan;
import android.text.style.TypefaceSpan;
import android.widget.EditText;

public class MainActivity extends Activity {

    char ZWJ_CHAR = '\u200B'; //'§'

    EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText) findViewById(R.id.edittext);

        mEditText.setText("b1\nb2");
        mEditText.addTextChangedListener(tw);

        mEditText.getText().setSpan(new TypefaceSpan("serif"), 0, mEditText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mEditText.getText().setSpan(new BulletSpan(), 0, 2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mEditText.getText().setSpan(new BulletSpan(), 3, 5, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

    TextWatcher tw = new TextWatcher() {
        private boolean gotNewline = false;
        private boolean rightAfterZwj = false;
        private char zwjNeighbohr = 0;
        private boolean deletedZwj = false;
        private int inputStart = 0;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            deletedZwj = (count == 1 && after == 0 && s.charAt(start) == ZWJ_CHAR);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            zwjNeighbohr = (start > 0 && count > 0 && s.charAt(start - 1) == ZWJ_CHAR) ? s.charAt(start) : 0;
            gotNewline = (count == 1 && s.charAt(start + count - 1) == '\n');
            rightAfterZwj = (start > 0 && count > 0 && s.charAt(start - 1) == ZWJ_CHAR);
            inputStart = start;
        }

        @Override
        public void afterTextChanged(Editable text) {
            // get the bullet at input position
            BulletSpan[] spans = text.getSpans(inputStart, inputStart + 1, BulletSpan.class);
            BulletSpan span = spans != null && spans.length > 0 ? spans[0] : null;
//            int itemStart = text.getSpanStart(span);
//            int itemEnd = text.getSpanEnd(span);

            if (span == null) {
                span = null;
            }
            if (gotNewline && span != null) {
                if (handleNewlineInList(span, inputStart, rightAfterZwj)) {
                    // add a ZWJ at the text end otherwise paragraph doesn't render when at end of text and empty
                    text.insert(inputStart + 1, "" + ZWJ_CHAR);
                }
            } else {
                if (inputStart > 0 && deletedZwj) {
                    // ZWJ got removed so, issue an extra delete to emulate bullet deletion
                    text.delete(inputStart - 1, inputStart);
                }

                // remove ZWJ if it got company. No longer needed
                if (zwjNeighbohr != 0) {
                    int zwjPosition = text.toString().indexOf(ZWJ_CHAR);
                    if (zwjPosition > -1) {
                        text.replace(zwjPosition, zwjPosition + 2, "" + zwjNeighbohr);
                    }
                }

//                if (itemStart > -1 && itemStart == itemEnd) {
//                    // add a ZWJ if bullet empty
//                    text.insert(inputStart, "" + ZWJ_CHAR);
//                }
            }
        }
    };

    boolean handleNewlineInList(BulletSpan span, int position, boolean rightAfterZwj) {
        Editable text = mEditText.getText();

        int itemStart = text.getSpanStart(span);
        int itemEnd = text.getSpanEnd(span);

        boolean addedNewBullet = false;

        if (itemStart == position) {
            // newline added at start of bullet so, push current bullet forward and add a new bullet in place
            text.setSpan(span, position + 1, itemEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            text.setSpan(new BulletSpan(), position, position, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            addedNewBullet = true;
        } else if (rightAfterZwj && itemEnd == position + 1) {
            // close the list when entering a newline oon an empty item at the end of the list
            text.removeSpan(span);
            text.delete(position - 1, position + 1);
        } else {
            // newline added at some position inside the bullet so, end the current bullet and append a new one
            text.setSpan(span, itemStart, position, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            text.setSpan(new BulletSpan(), position + 1, itemEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            addedNewBullet = true;
        }

        // return true if we added a new bullet at the end of the list
        return addedNewBullet && position + 1 == itemEnd;
    }
}
