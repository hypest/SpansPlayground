package org.hypest.spansplayground;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StrikethroughSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.UnderlineSpan;
import android.widget.EditText;

public class MainActivity extends Activity {

    EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText) findViewById(R.id.edittext);

        mEditText.setText("Exampletext");
        mEditText.addTextChangedListener(tw);

        mEditText.getText().setSpan(new SuperscriptSpan(), 0, 7, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        mEditText.getText().setSpan(new SubscriptSpan(), 8, mEditText.getText().length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        mEditText.getText().setSpan(new StrikethroughSpan(), 0, mEditText.getText().length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
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
                StrikethroughSpan span = mEditText.getText().getSpans(position, position, StrikethroughSpan.class)[0];
                mEditText.getText().setSpan(span, mEditText.getText().getSpanStart(span), position, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                mEditText.getText().setSpan(new UnderlineSpan(), position, position + 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }
    };
}
