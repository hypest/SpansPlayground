package org.hypest.spansplayground;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BulletSpan;
import android.widget.EditText;

public class MainActivity extends Activity {

    EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText) findViewById(R.id.edittext);

        mEditText.setText("b1\nb2");
        mEditText.addTextChangedListener(tw);

        mEditText.getText().setSpan(new BulletSpan(), 0, 2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mEditText.getText().setSpan(new BulletSpan(), 3, 5, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

    TextWatcher tw = new TextWatcher() {
        private boolean gotNewline = false;
        private int position = 0;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            gotNewline = (count == 1 && s.charAt(start + count - 1) == '\n');
            position = start;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (gotNewline) {
                // get the bullet at newline position
                BulletSpan span = mEditText.getText().getSpans(position, position, BulletSpan.class)[0];

                if (mEditText.getText().getSpanStart(span) == position) {
                    // newline added at start of bullet so, push current forward and add a new bullet in place
                    mEditText.getText().setSpan(span, position + 1, mEditText.getText().getSpanEnd(span), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    mEditText.getText().setSpan(new BulletSpan(), position, position, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    // newline added at some position inside the bullet so, end the current bullet and append a new one
                    mEditText.getText().setSpan(span, mEditText.getText().getSpanStart(span), position, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    mEditText.getText().setSpan(new BulletSpan(), position + 1, position + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }
        }
    };
}
