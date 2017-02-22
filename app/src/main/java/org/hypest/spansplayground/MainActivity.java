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
            handleTextChangeForLists(mEditText.getText(), textChangedEvent);
        }
    };

    // return true if newline got handled by the list
    boolean handleTextChangeForLists(Editable text, TextChangedEvent event) {
        // use charsNew to get the spans at the input point. It appears to be more reliable vs the whole Editable.
        TypefaceSpan[] lists = event.charsNew.getSpans(0, 0, TypefaceSpan.class);
        if (lists == null || lists.length == 0) {
            // no list so, nothing to do here
            return false;
        }

        TypefaceSpan list = lists[0]; // TODO: handle nesting

        BulletSpan[] listItems = event.charsNew.getSpans(0, 0, BulletSpan.class);
        BulletSpan listItem = listItems != null && listItems.length > 0 ? listItems[0] : null;

        if (event.gotNewline) {
            return handleNewlineInList(text, list, listItem, event.inputStart, event.gotNewlineAfterZwj);
        }

        if (event.deletedZwj) {
            return handleZwjDeletionInList(text, event.inputStart, event.charsOld, event.charsNew, list);
        }

        if (event.zwjRightNeighbor != 0) {
            // ZWJ got company after it so, it's no longer needed
            SpansHelper.deleteAndIgnore(text, event.inputStart - 1, 1);
            return true;
        }

        if (event.deletedNewline) {
            return handleNewlineDeletionInList(text, event.inputStart, event.charsOld, list, listItems);
        }

        if (event.charsNew.length() == 0) {
            // text was removed so, let's make sure the listitem has a ZWJ
            int itemStart = text.getSpanStart(listItem);
            int itemEnd = text.getSpanEnd(listItem);

            if (itemStart == itemEnd) {
                // add a ZWJ if bullet empty
                SpansHelper.insertZwj(text, event.inputStart);
                return true;
            }
        }

        return false;
    }

    // return true if newline got handled by the list
    boolean handleNewlineInList(Editable text, TypefaceSpan list, BulletSpan listItem, int newlineIndex,
            boolean rightAfterZwj) {
        if (listItem == null) {
            // no list so, nothing to do here
            return false;
        }

        int listStart = text.getSpanStart(list);
        int listEnd = text.getSpanEnd(list);

        int itemStart = text.getSpanStart(listItem);
        int itemEnd = text.getSpanEnd(listItem);

        if (newlineIndex == itemStart) {
            // newline added at start of bullet so, push current bullet forward and add a new bullet in place
            SpansHelper.setListItem(text, listItem, newlineIndex + 1, itemEnd);

            SpansHelper.insertZwj(text, newlineIndex);

            SpansHelper.newListItem(text, newlineIndex, newlineIndex + 1);
            return true;
        }

        if (rightAfterZwj && newlineIndex == listEnd - 1) {
            // close the list when entering a newline on an empty item at the end of the list
            text.removeSpan(listItem);

            if (listEnd - listStart == 2) {
                // list only has the empty list item so, remove the list itself as well!
                text.removeSpan(list);
            } else {
                // adjust the list end
                SpansHelper.setList(text, list, listStart, newlineIndex - 2);
            }

            // delete the newline and the ZWJ before it
            SpansHelper.deleteAndIgnore(text, newlineIndex - 1, 2);
            return true;
        }

        if (newlineIndex == itemEnd -1){
            // newline added at the end of the list item so, adjust the leading one to not include the newline and
            //  add append new list item
            SpansHelper.setListItem(text, listItem, itemStart, newlineIndex);

            // it's going to be an empty list item so, add a ZWJ to make the bullet render
            SpansHelper.insertZwj(text, newlineIndex + 1);

            // append a new list item span and include the ZWJ in it
            SpansHelper.newListItem(text, newlineIndex + 1, itemEnd + 1);
            return true;
        }

        {
            // newline added at some position inside the bullet so, end the current bullet and append a new one
            SpansHelper.setListItem(text, listItem, itemStart, newlineIndex);
            SpansHelper.newListItem(text, newlineIndex + 1, itemEnd);
            return true;
        }
    }

    boolean handleZwjDeletionInList(Editable text, int inputStart, Spanned charsOld, Spanned charsNew, TypefaceSpan list) {
        if (SpansHelper.handledDeletionIgnore(text, charsOld)) {
            // let it go. This ZWJ deletion is deliberate, happening when we're joining a list item with an empty one
            return false;
        }

        // ZWJ just got deleted. Unfortunately, we need to manually remove the dangling list item span (a side
        //  effect of SPAN_INCLUSIVE_INCLUSIVE). Fortunately, it's right there at the start of charsNew :)
        text.removeSpan(charsNew.getSpans(0, 0, BulletSpan.class)[0]);

        int listStart = text.getSpanStart(list);
        int listEnd = text.getSpanEnd(list);

        if (listStart == listEnd) {
            // list just got empty so, remove it
            text.removeSpan(list);
        } else {
            if (inputStart == listStart) {
                // deleting the very first line item so, need to push the list down
                SpansHelper.setList(text, list, inputStart + 1, listEnd);
            }

            if (inputStart > listStart) {
                // with ZWJ deleted, let's delete the newline before it, effectively deleting the line.
                SpansHelper.deleteAndIgnore(text, inputStart - 1, 1);
            }
        }

        return true;
    }

    boolean handleNewlineDeletionInList(Editable text, int inputStart, Spanned charsOld, TypefaceSpan list,
            BulletSpan[] listItems) {
        if (SpansHelper.handledDeletionIgnore(text, charsOld)) {
            // let it go. This newline deletion was deliberate, happening when we're joining a list item with an empty one
            return false;
        }

        // a newline adjacent or inside the list got deleted

        int listStart = text.getSpanStart(list);
        int listEnd = text.getSpanEnd(list);

        BulletSpan leadingItem;
        BulletSpan trailingItem;

        if (inputStart == listEnd) {
            // we're joining text _into_ the list at its end so,
            leadingItem = listItems[0];
            trailingItem = null;
        } else if (inputStart == listStart) {
            // we're extracting the first list item out of the list, into the text before it so,
            leadingItem = null;
            trailingItem = listItems[0];
        } else {
            // newline was separating two list items so, need to join those two, giving priority to the left most one.
            leadingItem = text.getSpanStart(listItems[0]) < text.getSpanStart(listItems[1]) ?
                    listItems[0] : listItems[1];
            trailingItem = text.getSpanEnd(listItems[0]) > text.getSpanStart(listItems[1]) ?
                    listItems[0] : listItems[1];
        }

        if (leadingItem == null) {
            // we're extracting from the list start so, need to push the list start to the start of the next item
            int nextItemStart = text.getSpanEnd(trailingItem) + 1; // +1 to cater for the trailing item's newline

            if (nextItemStart < listEnd) {
                // push the list start to the start of the next item
                SpansHelper.setList(text, list, nextItemStart, listEnd);
            } else {
                // hmm, there's no next item actually... just remove the list!
                text.removeSpan(list);
            }

            // we're extracting from the list start so, the right-side item will drop out of the list
            text.removeSpan(trailingItem);
        } else {
            // we're joining text to the list

            int start = text.getSpanStart(leadingItem);
            int end;
            if (trailingItem != null) {
                end = text.getSpanEnd(trailingItem);
            } else {
                int nextNewlineIndex = text.toString().indexOf('\n', start);
                end = nextNewlineIndex != -1 ? nextNewlineIndex : text.length();
            }

            // adjust the leading item span to include both items' content
            SpansHelper.setListItem(text, leadingItem, start, end);

            // just remove the trailing list item span. We've given the leading one the priority.
            text.removeSpan(trailingItem);

            if (trailingItem == null) {
                // since we're joining text into the list at its end, let's expand the list span to include the new text
                SpansHelper.setList(text, list, listStart, end);
            }

            if (text.charAt(start) == Constants.ZWJ_CHAR) {
                // looks like we just joined into an empty list item so, let's remove the orphan ZWJ of the leading item
                SpansHelper.deleteAndIgnore(text, start, 1);
            }
        }

        if (inputStart < text.length() && text.charAt(inputStart) == Constants.ZWJ_CHAR) {
            // looks like the right side item was empty. Let's remove the orphan ZWJ of the trailing item.
            SpansHelper.deleteAndIgnore(text, inputStart, 1);
        }

        return true;
    }
}
