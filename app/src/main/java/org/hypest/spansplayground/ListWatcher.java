package org.hypest.spansplayground;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.EditText;

import java.lang.ref.WeakReference;

class ListWatcher implements TextWatcher {
    static void install(EditText text) {
        text.addTextChangedListener(new ListWatcher(new ListHandler(text.getText()), text));
    }

    private final WeakReference<EditText> editTextRef;
    private final ListHandler listHandler;

    private ListHandler.TextDeleter textDeleter = new ListHandler.TextDeleter() {
        @Override
        public void delete(final int start, final int end) {
            if (editTextRef.get() != null) {
                editTextRef.get().post(new Runnable() {
                    @Override
                    public void run() {
                        if (editTextRef.get() != null) {
                            editTextRef.get().getText().delete(start, end);
                        }
                    }
                });
            }
        }
    };

    private ListWatcher(ListHandler listHandler, EditText editText) {
        this.listHandler = listHandler;
        editTextRef = new WeakReference<>(editText);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // handle the text change. The potential text deletion will happen in a scheduled Runnable, to run on next frame
        listHandler.handleTextChangeForLists(
                (Spannable) s,
                start,
                (Spanned) s.subSequence(start, start + count), // subsequence seems more reliable vs the whole Editable.
                textDeleter);
    }

    @Override
    public void afterTextChanged(Editable text) {}
}