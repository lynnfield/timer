package com.lynnfield.timer;

import android.support.v7.widget.RecyclerView;
import android.view.View;


public class CyclicLayoutManager extends RecyclerView.LayoutManager {
    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        fillDown(recycler);
    }

    private void fillDown(final RecyclerView.Recycler recycler) {
        int pos = 0;
        int viewTop = 0;
        boolean fillDown = true;

        while (fillDown && pos < getItemCount()) {
            final View v = recycler.getViewForPosition(pos);
            addView(v);
            measureChildWithMargins(v, 0, 0);
            layoutDecorated(v, 0, viewTop, getDecoratedMeasuredWidth(v), getDecoratedMeasuredHeight(v));
            viewTop = getDecoratedBottom(v);
            fillDown = viewTop <= getHeight();
            ++pos;
        }
    }
}
