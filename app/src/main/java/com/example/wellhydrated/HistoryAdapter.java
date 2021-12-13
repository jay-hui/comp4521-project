package com.example.wellhydrated;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>{
    private final Cursor data;
    private LayoutInflater mInflater;

    public HistoryAdapter(Context context, Cursor cursor) {
        mInflater = LayoutInflater.from(context);
        this.data = cursor;
    }

    @NonNull
    @Override
    public HistoryAdapter.HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.history_item, parent, false);
        return new HistoryViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.HistoryViewHolder holder, int position) {
        if (!data.moveToPosition(position)) {
            // TODO: Item not found, error?
            return;
        }
        String mCurrent = data.getString(0) + "\t\t\t\t" + data.getString(1) + "ml";
        holder.historyItemView.setText(mCurrent);
    }

    @Override
    public int getItemCount() {
        return data.getCount();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        public final TextView historyItemView;
        final HistoryAdapter mAdapter;

        public HistoryViewHolder(@NonNull View itemView, HistoryAdapter adapter) {
            super(itemView);
            historyItemView = itemView.findViewById(R.id.historyText);
            this.mAdapter = adapter;
        }
    }
}
