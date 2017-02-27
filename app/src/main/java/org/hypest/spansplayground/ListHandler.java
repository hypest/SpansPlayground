package org.hypest.spansplayground;

import android.text.Spannable;
import android.text.Spanned;

class ListHandler {
    interface TextDeleter {
        void delete(final int start, final int end);
    }

    private enum PositionType {
        LIST_START,
        EMPTY_ITEM_AT_LIST_END,
        TEXT_END,
        LIST_ITEM_BODY
    }

    private Spannable text;

    ListHandler(Spannable text) {
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

    void handleTextChangeForLists(Spannable text, int inputStart, int count, TextDeleter textDeleter) {
        // use charsNew to get the spans at the input point. It appears to be more reliable vs the whole Editable.
        Spanned charsNew = (Spanned) text.subSequence(inputStart, inputStart + count);

        ListSpan[] lists = charsNew.getSpans(0, 0, ListSpan.class);
        if (lists == null || lists.length == 0) {
            // no lists so, bail.
            return;
        }

        SpanWrapper<ListSpan> list = new SpanWrapper<>(text, lists[0]); // TODO: handle nesting

        String charsNewString = charsNew.toString();
        int newlineOffset = charsNewString.indexOf(Constants.NEWLINE);
        while (newlineOffset > -1 && newlineOffset < charsNew.length()) {
            int newlineIndex = inputStart + newlineOffset;

            // re-subsequence to get the newer state of the spans
            charsNew = (Spanned) text.subSequence(inputStart, inputStart + count);
            ListItemSpan[] listItems = charsNew.getSpans(newlineOffset, newlineOffset, ListItemSpan.class);
            SpanWrapper<ListItemSpan> item = listItems != null && listItems.length > 0 ?
                    new SpanWrapper<>(text, listItems[0]) : null;

            switch (getNewlinePositionType(text, list, item, newlineIndex)) {
                case LIST_START:
                    handleNewlineAtListStart(item, newlineIndex);
                    break;
                case EMPTY_ITEM_AT_LIST_END:
                    handleNewlineAtEmptyItemAtListEnd(list, item, newlineIndex, textDeleter);
                    break;
                case TEXT_END:
                    handleNewlineAtTextEnd();
                    break;
                case LIST_ITEM_BODY:
                    handleNewlineInListItemBody(item, newlineIndex);
                    break;
            }

            newlineOffset = charsNewString.indexOf(Constants.NEWLINE, newlineOffset + 1);
        }

        boolean gotEndOfBufferMarker = (charsNew.length() == 1 && charsNew.charAt(0) == Constants.END_OF_BUFFER_MARKER);
        if (gotEndOfBufferMarker) {
            handleEndOfBufferInList(text, inputStart);
        }
    }

    private PositionType getNewlinePositionType(Spannable text, SpanWrapper<ListSpan> list,
            SpanWrapper<ListItemSpan> item, int newlineIndex) {
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
            int newlineIndex, TextDeleter textDeleter) {
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
        textDeleter.delete(newlineIndex, newlineIndex + 1);
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

    private boolean handleEndOfBufferInList(Spannable text, int markerIndex) {
        ListItemSpan[] listItems = text.getSpans(markerIndex, markerIndex + 1, ListItemSpan.class);
        SpanWrapper<ListItemSpan> item = listItems != null && listItems.length > 0 ?
                new SpanWrapper<>(text, listItems[0]) : null;

        if (item.getStart() == markerIndex) {
            // ok, this list item has the marker as its first char so, nothing more to do. Bail.
            return false;
        }

        // attach a new bullet around the end-of-text marker
        newListItem(markerIndex, markerIndex + 1);

        // the list item has bled over to the marker so, let's adjust its range to just before the marker. There's a
        //  newline there hopefully :)
        item.setEnd(markerIndex);

        return true;
    }
}
