package org.hypest.spansplayground;

interface ParagraphFlagged {
    int getStartBeforeCollapse();
    void setStartBeforeCollapse(int startBeforeCollapse);
    void clearStartBeforeCollapse();
    boolean hasCollapsed();

    int getEndBeforeBleed();
    void setEndBeforeBleed(int endBeforeBleed);
    void clearEndBeforeBleed();
    boolean hasBled();
}
