package org.hypest.spansplayground;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
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

        SpansHelper.newList(mEditText.getText(), 0, mEditText.length());
        SpansHelper.newListItem(mEditText.getText(), 0, 2);
        SpansHelper.newListItem(mEditText.getText(), 3, 5);
    }

    TextWatcher tw = new TextWatcher() {
        private int inputStart;
        private Spanned charsOld;
        private Spanned charsNew;

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
            TextChangedEvent textChangedEvent = new TextChangedEvent(text, inputStart, charsOld, charsNew);
            ListHelper.handleTextChangeForLists(mEditText.getText(), textChangedEvent);
        }
    };
}
