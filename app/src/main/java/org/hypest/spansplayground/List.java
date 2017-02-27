package org.hypest.spansplayground;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.EditText;

class List implements TextWatcher {
    static void install(EditText text) {
        text.addTextChangedListener(new List(text.getText()));
    }

    private enum PositionType {
        LIST_START,
        EMPTY_ITEM_AT_LIST_END,
        TEXT_END,
        LIST_ITEM_BODY
    }

    private Editable text;

    private int inputStart;
    private Spanned charsNew;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        inputStart = start;
        charsNew = (Spanned) s.subSequence(start, start + count);
    }

    @Override
    public void afterTextChanged(Editable text) {
        // NOTE: According to the documentation, by the time this afterTextChanged have been called, the text might
        //  have been changed by other TextWatcher's afterTextChanged calls. This might introduce some inconsistency
        //  and "random" bugs.

        // use charsNew to get the spans at the input point. It appears to be more reliable vs the whole Editable.
        ListSpan[] lists = charsNew.getSpans(0, 0, ListSpan.class);
        if (lists != null && lists.length > 0) {
            handleTextChangeForLists(lists);
        }
    }

    private List(Editable text) {
        this.text = text;
    }

    static void newList(Spannable text, int start, int end) {
        text.setSpan(new ListSpan(), start, end, Spanned.SPAN_PARAGRAPH);
    }

    static void newListItem(Spannable text, int start, int end) {
        text.setSpan(new ListItemSpan(), start, end, Spanned.SPAN_PARAGRAPH);
    }

    private void newListItem(int start, int end) {
        newListItem(text, start, end);
    }

    private void handleTextChangeForLists(ListSpan[] lists) {
        SpanWrapper<ListSpan> list = new SpanWrapper<>(text, lists[0]); // TODO: handle nesting

        ListItemSpan[] listItems = charsNew.getSpans(0, 0, ListItemSpan.class);
        SpanWrapper<ListItemSpan> item = listItems != null && listItems.length > 0 ?
                new SpanWrapper<>(text, listItems[0]) : null;

        boolean gotNewline = (charsNew.length() == 1 && charsNew.charAt(0) == Constants.NEWLINE)
                || (charsNew.length() == 2 && charsNew.charAt(0) == Constants.NEWLINE
                && charsNew.charAt(1) == Constants.END_OF_BUFFER_MARKER);
        if (gotNewline) {
            switch (getNewlinePositionType(list, item, inputStart)) {
                case LIST_START:
                    handleNewlineAtListStart(item, inputStart);
                    break;
                case EMPTY_ITEM_AT_LIST_END:
                    handleNewlineAtEmptyItemAtListEnd(list, item, inputStart);
                    break;
                case TEXT_END:
                    handleNewlineAtTextEnd();
                    break;
                case LIST_ITEM_BODY:
                    handleNewlineInListItemBody(item, inputStart);
                    break;
            }
        }

        boolean gotEndOfBufferMarker = (charsNew.length() == 1 && charsNew.charAt(0) == Constants.END_OF_BUFFER_MARKER);
        if (gotEndOfBufferMarker) {
            handleEndOfBufferInList(item, inputStart);
        }
    }

    private PositionType getNewlinePositionType(SpanWrapper<ListSpan> list, SpanWrapper<ListItemSpan> item,
            int newlineIndex) {
        boolean atEndOfList = newlineIndex == list.getEnd() - 2 || newlineIndex == text.length() - 1;

        if (newlineIndex == item.getStart() && !atEndOfList) {
            return PositionType.LIST_START;
        }

        if (newlineIndex == item.getStart() && atEndOfList) {
            return PositionType.EMPTY_ITEM_AT_LIST_END;
        }

        if (newlineIndex == text.length() - 1) {
            return PositionType.TEXT_END;
        }

        // no special case applied so, newline is in the "body" of the bullet
        return PositionType.LIST_ITEM_BODY;
    }

    private void handleNewlineAtListStart(SpanWrapper<ListItemSpan> item, int newlineIndex) {
        // newline added at start of bullet so, add a new bullet
        newListItem(newlineIndex, newlineIndex + 1);

        // push current bullet forward
        item.setStart(newlineIndex + 1);
    }

    private void handleNewlineAtEmptyItemAtListEnd(SpanWrapper<ListSpan> list, SpanWrapper<ListItemSpan> item,
            int newlineIndex) {
        // close the list when entering a newline on an empty item at the end of the list
        item.remove();

        if (list.getEnd() - list.getStart() == 1) {
            // list only has the empty list item so, remove the list itself as well!
            list.remove();
        } else {
            // adjust the list end to only include the chars before the newline just added
            list.setEnd(newlineIndex);
        }

        // delete the newline
        text.delete(newlineIndex, newlineIndex + 1);
    }

    private void handleNewlineAtTextEnd() {
        // got a newline while being at the end-of-text. We'll let the current list item engulf it and will wait
        //  for the end-of-text marker event in order to attach the new list item to it when that happens.

        // no-op here
    }

    private void handleNewlineInListItemBody(SpanWrapper<ListItemSpan> item, int newlineIndex) {
        // newline added at some position inside the bullet so, end the current bullet and append a new one
        newListItem(newlineIndex + 1, item.getEnd());
        item.setEnd(newlineIndex + 1);
    }

    private boolean handleEndOfBufferInList(SpanWrapper<ListItemSpan> listItem, int markerIndex) {
        if (listItem.getStart() == markerIndex) {
            // ok, this list item has the marker as its first char so, nothing more to do. Bail.
            return false;
        }

        // attach a new bullet around the end-of-text marker
        newListItem(markerIndex, markerIndex + 1);

        // the list item has bled over to the marker so, let's adjust its range to just before the marker. There's a
        //  newline there hopefully :)
        listItem.setEnd(markerIndex);

        return true;
    }
}
