package org.hypest.spansplayground;

import android.text.Editable;

class ListHelper {
    // return true if newline got handled by the list
    static boolean handleTextChangeForLists(Editable text, TextChangedEvent event) {
        // use charsNew to get the spans at the input point. It appears to be more reliable vs the whole Editable.
        ListSpan[] lists = event.charsNew.getSpans(0, 0, ListSpan.class);
        if (lists == null || lists.length == 0) {
            // no list so, nothing to do here
            return false;
        }

        ListSpan list = lists[0]; // TODO: handle nesting

        ListItemSpan[] listItems = event.charsNew.getSpans(0, 0, ListItemSpan.class);
        ListItemSpan listItem = listItems != null && listItems.length > 0 ? listItems[0] : null;

        if (event.gotNewline) {
            return handleNewlineInList(text, list, listItem, event.inputStart);
        }

        if (event.gotEndOfBufferMarker) {
            return handleEndOfBufferInList(text, event, list, listItem, event.inputStart);
        }

        return false;
    }

    // return true if newline got handled by the list
    private static boolean handleNewlineInList(Editable text, ListSpan list, ListItemSpan listItem, int newlineIndex) {
        int listStart = text.getSpanStart(list);
        int listEnd = text.getSpanEnd(list);

        int itemStart = text.getSpanStart(listItem);
        int itemEnd = text.getSpanEnd(listItem);

        boolean atEndOfList = newlineIndex == listEnd - 2 || newlineIndex == text.length() - 1;

        if (newlineIndex == itemStart && !atEndOfList) {
            // newline added at start of bullet so, push current bullet forward and add a new bullet in place
            SpansHelper.setListItem(text, listItem, newlineIndex + 1, itemEnd);

            SpansHelper.newListItem(text, newlineIndex, newlineIndex + 1);
            return true;
        }

        if (newlineIndex == itemStart && atEndOfList) {
            // close the list when entering a newline on an empty item at the end of the list
            text.removeSpan(listItem);

            if (listEnd - listStart == 1) {
                // list only has the empty list item so, remove the list itself as well!
                text.removeSpan(list);
            } else {
                // adjust the list end to only include the chars before the newline just added
                SpansHelper.setList(text, list, listStart, newlineIndex);
            }

            // delete the newline
            text.delete(newlineIndex, newlineIndex + 1);
            return true;
        }

        if (newlineIndex == itemEnd - 2) {
            // newline added at the end of the bullet so, adjust the bullet to end at the new newline.
            //  Note: there's already a newline at the bullet end, hence the "-2" in the condition instead of "-1".
            SpansHelper.setListItem(text, listItem, itemStart, newlineIndex + 1);

            // append a new list item span
            SpansHelper.newListItem(text, newlineIndex + 1, itemEnd);
            return true;
        }

        if (newlineIndex == text.length() - 1) {
            // got a newline while being at the end-of-text. We'll let the current list item engulf it and will wait
            //  for the end-of-text marker event in order to attach the new list item to it when that happens.
            return true;
        }

        {
            // newline added at some position inside the bullet so, end the current bullet and append a new one
            SpansHelper.setListItem(text, listItem, itemStart, newlineIndex + 1);
            SpansHelper.newListItem(text, newlineIndex + 1, itemEnd);
            return true;
        }
    }

    private static boolean handleEndOfBufferInList(Editable text, TextChangedEvent event, ListSpan list,
            ListItemSpan listItem, int markerIndex) {
        int itemStart = text.getSpanStart(listItem);

        if (itemStart == markerIndex) {
            // ok, this list item has the marker as its first char so, nothing more to do. Bail.
            return false;
        }

        // ok, this list item has bled over to the marker so, let's adjust its range to just before the marker. There's
        //  a newline there hopefully :)
        SpansHelper.setListItem(text, listItem, itemStart, markerIndex);

        // attach a new bullet around the end-of-text marker
        SpansHelper.newListItem(text, markerIndex, markerIndex + 1);

        return true;
    }
}
