package org.hypest.spansplayground;

import android.app.Activity;
import android.os.Bundle;
import android.text.Spanned;
import android.widget.EditText;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editText = (EditText) findViewById(R.id.edittext);

        editText.setText("pro\nb1\nb2\naft");

        SpansHelper.newList(editText.getText(), 4, 10, Spanned.SPAN_PARAGRAPH);
        SpansHelper.newListItem(editText.getText(), 4, 7);
        SpansHelper.newListItem(editText.getText(), 7, 10);

        EndOfBufferMarkerAdder.install(editText);
        ListGarbageCollection.install(editText.getText());
        ListHandler.install(editText);
    }
}
