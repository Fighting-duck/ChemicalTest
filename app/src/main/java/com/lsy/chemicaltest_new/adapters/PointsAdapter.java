package com.lsy.chemicaltest_new.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.lsy.chemicaltest_new.R;

public class PointsAdapter extends ListAdapter<Double, PointsAdapter.ViewHolder> {
    public interface OnDeleteValueListener {
        void onDelete(int position);
    }
    public interface OnAlterValueListener {
        void onAlter(int position);
    }
    private OnDeleteValueListener mDeleteValueListener;
    private OnAlterValueListener mAlterValueListener;
    public void setOnDeleteValueListener(OnDeleteValueListener listener) {
        mDeleteValueListener = listener;
    }
    public void setOnAlterValueListener(OnAlterValueListener listener) {
        mAlterValueListener = listener;
    }
    private static final DiffUtil.ItemCallback<Double> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Double>() {
                @Override
                public boolean areItemsTheSame(@NonNull Double oldItem, @NonNull Double newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Double oldItem, @NonNull Double newItem) {
                    return oldItem.equals(newItem);
                }
            };
    public PointsAdapter() {
        super(DIFF_CALLBACK);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView positionTextView, valueTextView;
        ImageButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            positionTextView = itemView.findViewById(R.id.tv_top);
            valueTextView = itemView.findViewById(R.id.tv_value);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.point_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Double item = getItem(position);
        if (position==0) holder.deleteButton.setVisibility(View.GONE);
        holder.positionTextView.setText("Y" + (position + 1));
        holder.valueTextView.setText(String.valueOf(item));
        holder.deleteButton.setOnClickListener(v -> mDeleteValueListener.onDelete(position));
        holder.valueTextView.setOnClickListener(v -> mAlterValueListener.onAlter(position));
    }
}
