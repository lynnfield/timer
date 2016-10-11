package com.lynnfield.timer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


class NumbersAdapter extends RecyclerView.Adapter<NumbersAdapter.ViewHolder> {

    private final int max;

    NumbersAdapter(final int max) {
        this.max = max;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final LayoutInflater i = LayoutInflater.from(parent.getContext());
        final View v = i.inflate(R.layout.item_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.tv.setText(String.valueOf(position % max));
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv;

        ViewHolder(final View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.text);
        }
    }
}
