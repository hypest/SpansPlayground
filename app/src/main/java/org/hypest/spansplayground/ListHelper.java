package org.hypest.spansplayground;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.TypefaceSpan;

class ListHelper {
    // return true if newline got handled by the list
    static boolean handleTextChangeForLists(Editable text, TextChangedEvent event) {
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
            return handleNewlineInList(text, event, list, listItem, event.inputStart);
        }

//        if (event.deletedNewline) {
//            return handleNewlineDeletionInList(text, event.inputStart, event.newlineIndex, event.charsOld, list,
//                    listItems);
//        }

        return false;
    }

    // return true if newline got handled by the list
    private static boolean handleNewlineInList(Editable text, TextChangedEvent event, TypefaceSpan list,
            BulletSpan listItem, int newlineIndex) {
        if (SpansHelper.handledIgnore(text, event.charsNew)) {
            // let it go. This newline change was deliberate, happening when expanding the list at end-of-text
            return false;
        }

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

                if (newlineIndex == text.length() - 1) {
                    // well, list was spanning to the end of text (which means we also had it open-ended) so, close it
                    //  now that there will be non-list text after the list
                    SpansHelper.closeListEnd(text, list);
                    SpansHelper.closeListItemEnd(text, listItem);
                }
            }

            // delete the newline
            SpansHelper.deleteAndIgnore(text, newlineIndex, 1);
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

//        if (newlineIndex == text.length() - 1) {
//            // attach a new bullet after the newline. We need to put the edge character manually (and spanned) here
//            //  because only setting a zero-width (paragraph) span and expecting it to expand to include the character
//            //  later doesn't seem to work (the span stays zero-width).
//            Spannable emptyEdge = new SpannableStringBuilder("" + Constants.END_OF_BUFFER_MARKER);
//            SpansHelper.newListItem(emptyEdge, 0, 1);
//            text.append(emptyEdge);
//
//            // re-set the current bullet's range since because of becoming greedy at the end-of-text it engulfed the
//            //  text-end marker as well. Let's push it back to only span after the newline char.
//            SpansHelper.setListItem(text, listItem, itemStart, newlineIndex + 1);
//
//            // list is no longer at the end-of-text so, close-end it
//            SpansHelper.closeListEnd(text, list);
//
//            return true;
//        }

        {
            // newline added at some position inside the bullet so, end the current bullet and append a new one
            SpansHelper.setListItem(text, listItem, itemStart, newlineIndex + 1);
            SpansHelper.newListItem(text, newlineIndex + 1, itemEnd);
            return true;
        }
    }

//    private static boolean handleNewlineDeletionInList(Editable text, int inputStart, int newlineIndex, Spanned charsOld,
//            TypefaceSpan list, BulletSpan[] listItems) {
//        if (SpansHelper.handledIgnore(text, charsOld)) {
//            // let it go. This newline change was deliberate, happening when closing the list
//            return false;
//        }
//
//        // a newline adjacent or inside the list got deleted
//
//        int listStart = text.getSpanStart(list);
//        int listEnd = text.getSpanEnd(list);
//
//        BulletSpan leadingItem;
//        BulletSpan trailingItem = null;
//
//        if (newlineIndex == listEnd) {
//            // we're joining text _into_ the list at its end so,
//            leadingItem = listItems[0];
//            trailingItem = null;
//        } else if (newlineIndex == listStart && !SpansHelper.hasList(charsOld, 0, 0)) {
//            // we're extracting the first list item out of the list, into the text before it so,
//            leadingItem = null;
//            trailingItem = listItems[0];
//        } else {
//            // newline was separating two list items so, need to join those two, giving priority to the left most one.
//
//            // before being deleted, the newline was attached to its bullet so, the bullet is in charsOld
//            leadingItem = charsOld.getSpans(0, 0, BulletSpan.class)[0];
//
//            // the right side one is the one starting at inputStart
//            for (BulletSpan item : listItems) {
//                if (text.getSpanStart(item) == inputStart) {
//                    trailingItem = item;
//                    break;
//                }
//            }
//        }
//
//        if (leadingItem == null) {
//            // we're extracting from the list start so, need to push the list start to the start of the next item
//            int nextItemStart = text.getSpanEnd(trailingItem);
//
//            if (nextItemStart < listEnd) {
//                // push the list start to the start of the next item
//                SpansHelper.setList(text, list, nextItemStart, listEnd);
//            } else {
//                // hmm, there's no next item actually... just remove the list!
//                text.removeSpan(list);
//            }
//
//            // we're extracting from the list start so, the right-side item will drop out of the list
//            text.removeSpan(trailingItem);
//        } else {
//            // we're joining text to the list
//
//            int start = text.getSpanStart(leadingItem);
//            int end;
//            if (trailingItem != null) {
//                end = text.getSpanEnd(trailingItem);
//            } else {
//                int nextNewlineIndex = text.toString().indexOf(Constants.NEWLINE, start + 1);
//
//                if (nextNewlineIndex == -1) {
//                    // no newline after the list so, need to expand the list to the end of text and open its end
//                    end = text.length();
//
//                    SpansHelper.openListEnd(text, list);
//                    SpansHelper.openListItemEnd(text, leadingItem);
//                } else {
//                    // we'll expand the list until the next newline, including the newline itself
//                    end = nextNewlineIndex + 1;
//                }
//            }
//
//            // adjust the leading item span to include both items' content
//            SpansHelper.setListItem(text, leadingItem, start, end);
//
//            // just remove the trailing list item span. We've given the leading one the priority.
//            text.removeSpan(trailingItem);
//
//            if (trailingItem == null) {
//                // since we're joining text into the list at its end, let's expand the list span to include the new text
//                SpansHelper.setList(text, list, listStart, end);
//            }
//        }
//
//        return true;
//    }
}
