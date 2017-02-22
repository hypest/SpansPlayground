package org.hypest.spansplayground;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BulletSpan;
import android.text.style.TypefaceSpan;
import android.widget.EditText;

public class MainActivity extends Activity {

    char NEWLINE = '\n';
    char ZWJ_CHAR = '\u200B';

    EditText mEditText;

    TypefaceSpan newList() {
        return new TypefaceSpan("serif");
    }

    void newList(Spannable text, int start, int end) {
        setList(text, newList(), start, end);
    }

    BulletSpan newListItem() {
        return new BulletSpan();
    }

    void newListItem(Spannable text, int start, int end) {
        setListItem(text, newListItem(), start, end);
    }

    void setList(Spannable text, TypefaceSpan list, int start, int end) {
        text.setSpan(list, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

    void setListItem(Spannable text, BulletSpan listItem, int start, int end) {
        text.setSpan(listItem, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

    void insertZwj(Editable text, int position) {
        text.insert(position, "" + ZWJ_CHAR);
    }

    void deleteAndIgnore(Editable text, int start, int count) {
        text.setSpan(new IgnoreDeletion(), start, start + count, Spanned.SPAN_COMPOSING);
        text.delete(start, start + count);
    }

    boolean handledDeletionIgnore(Spannable text, Spanned textFragment) {
        IgnoreDeletion[] allowDeletions = textFragment.getSpans(0, 0, IgnoreDeletion.class);
        if (allowDeletions != null && allowDeletions.length > 0) {
            // remove the marking span, it's job is finished.
            text.removeSpan(allowDeletions[0]);
            return true;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText) findViewById(R.id.edittext);

        mEditText.setText("b1\nb2");
        mEditText.addTextChangedListener(tw);

        newList(mEditText.getText(), 0, mEditText.length());
        newListItem(mEditText.getText(), 0, 2);
        newListItem(mEditText.getText(), 3, 5);
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
            handleTextChangeForLists(mEditText.getText(), inputStart, charsOld, charsNew);
        }
    };

    // return true if newline got handled by the list
    boolean handleTextChangeForLists(Editable text, int inputStart, Spanned charsOld, Spanned charsNew) {
        // use charsNew to get the spans at the input point. It appears to be more reliable vs the whole Editable.
        TypefaceSpan[] lists = charsNew.getSpans(0, 0, TypefaceSpan.class);
        if (lists == null || lists.length == 0) {
            // no list so, nothing to do here
            return false;
        }

        TypefaceSpan list = lists[0]; // TODO: handle nesting
        int listStart = text.getSpanStart(list);
        int listEnd = text.getSpanEnd(list);

        BulletSpan[] listItems = charsNew.getSpans(0, 0, BulletSpan.class);
        BulletSpan listItem = listItems != null && listItems.length > 0 ? listItems[0] : null;

        boolean gotNewline =
                charsNew.length() == 1
                && charsNew.charAt(0) == NEWLINE;

        char zwjRightNeighbor =
                (inputStart > 0
                        && charsNew.length() > 0
                        && text.charAt(inputStart - 1) == ZWJ_CHAR) ? charsNew.charAt(0) : 0;

        boolean deletedZwj =
                charsOld.length() == 1
                && charsNew.length() == 0
                && charsOld.charAt(0) == ZWJ_CHAR;

        if (gotNewline) {
            return handleNewlineInList(text, list, listItem, inputStart, zwjRightNeighbor == NEWLINE);
        }

        if (deletedZwj) {
            if (handledDeletionIgnore(text, charsOld)) {
                // let it go. This ZWJ deletion is deliberate, happening when we're joining a list item with an empty one
                return false;
            }

            // ZWJ just got deleted. Unfortunately, we need to manually remove the dangling list item span (a side
            //  effect of SPAN_INCLUSIVE_INCLUSIVE). Fortunately, it's right there at the start of charsNew :)
            text.removeSpan(charsNew.getSpans(0, 0, BulletSpan.class)[0]);

            if (list != null) {
                if (listStart == listEnd) {
                    // list just got empty so, remove it
                    text.removeSpan(list);
                } else {
                    if (inputStart == listStart) {
                        // deleting the very first line item so, need to push the list down
                        setList(text, list, inputStart + 1, listEnd);
                    }

                    if (inputStart > listStart) {
                        // with ZWJ deleted, let's delete the newline before it, effectively deleting the line.
                        deleteAndIgnore(text, inputStart - 1, 1);
                    }
                }
            }

            return true;
        }

        if (zwjRightNeighbor != 0) {
            // ZWJ got company after it so, it's no longer needed
            deleteAndIgnore(text, inputStart - 1, 1);
            return true;
        }

        boolean deletedNewline =
                charsOld.length() == 1
                        && charsNew.length() == 0
                        && charsOld.charAt(0) == NEWLINE;
        if (deletedNewline) {
            if (handledDeletionIgnore(text, charsOld)) {
                // let it go. This newline deletion was deliberate, happening when we're joining a list item with an empty one
                return false;
            }

            // a newline adjacent or inside the list got deleted

            BulletSpan leadingItem;
            BulletSpan trailingItem;

            if (inputStart == listEnd) {
                // we're joining text _into_ the list at its end so,
                leadingItem = listItems[0];
                trailingItem = null;
            } else {
                // newline was separating two list items so, need to join those two, giving priority to the left most one.
                leadingItem = text.getSpanStart(listItems[0]) < text.getSpanStart(listItems[1]) ?
                        listItems[0] : listItems[1];
                trailingItem = text.getSpanEnd(listItems[0]) > text.getSpanStart(listItems[1]) ?
                        listItems[0] : listItems[1];
            }

            int start = text.getSpanStart(leadingItem);
            int end;
            if (trailingItem != null) {
                end = text.getSpanEnd(trailingItem);
            } else {
                int nextNewlineIndex = text.toString().indexOf('\n', start);
                end = nextNewlineIndex != -1 ? nextNewlineIndex : text.length();
            }

            // adjust the leading item span to include both items' content
            setListItem(text, leadingItem, start, end);

            // just remove the trailing list item span. We've given the leading one the priority.
            text.removeSpan(trailingItem);

            if (trailingItem == null) {
                // since we're joining text into the list at its end, let's expand the list span to include the new text
                setList(text, list, listStart, end);
            }

            if (text.charAt(start) == ZWJ_CHAR) {
                // looks like we just joined into an empty list item so, let's remove the orphan ZWJ of the leading item
                deleteAndIgnore(text, start, 1);
            }

            if (text.charAt(inputStart) == ZWJ_CHAR) {
                // looks like we just joined an empty list item so, let's remove the orphan ZWJ of the trailing item
                deleteAndIgnore(text, inputStart, 1);
            }

            return true;
        }

        if (charsNew.length() == 0) {
            // text was removed so, let's make sure the listitem has a ZWJ
            int itemStart = text.getSpanStart(listItem);
            int itemEnd = text.getSpanEnd(listItem);

            if (itemStart == itemEnd) {
                // add a ZWJ if bullet empty
                insertZwj(text, inputStart);
                return true;
            }
        }

        return false;
    }

    static class IgnoreDeletion {}

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
            setListItem(text, listItem, newlineIndex + 1, itemEnd);

            insertZwj(text, newlineIndex);

            newListItem(text, newlineIndex, newlineIndex + 1);
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
                setList(text, list, listStart, newlineIndex - 2);
            }

            // delete the newline and the ZWJ before it
            deleteAndIgnore(text, newlineIndex - 1, 2);
            return true;
        }

        if (newlineIndex == itemEnd -1){
            // newline added at the end of the list item so, adjust the leading one to not include the newline and
            //  add append new list item
            setListItem(text, listItem, itemStart, newlineIndex);

            // it's going to be an empty list item so, add a ZWJ to make the bullet render
            insertZwj(text, newlineIndex + 1);

            // append a new list item span and include the ZWJ in it
            newListItem(text, newlineIndex + 1, itemEnd + 1);
            return true;
        }

        {
            // newline added at some position inside the bullet so, end the current bullet and append a new one
            setListItem(text, listItem, itemStart, newlineIndex);
            newListItem(text, newlineIndex + 1, itemEnd);
            return true;
        }
    }
}
