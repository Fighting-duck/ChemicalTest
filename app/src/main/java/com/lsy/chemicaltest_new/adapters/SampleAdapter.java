package com.lsy.chemicaltest_new.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.utils.PhotoUtil;

import java.util.List;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SampleAdapter extends ListAdapter<Sample, SampleAdapter.SampleViewHolder> {

    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    private OnImageClickListener onImageClickListener;
    private OnRelationshipClickListener onRelationshipClickListener;

    public SampleAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }
    public void setOnImageClickListener(OnImageClickListener listener) {
        this.onImageClickListener = listener;
    }
    public void setOnRelationshipClickListener(OnRelationshipClickListener listener) {
        this.onRelationshipClickListener = listener;
    }

    @NonNull
    @Override
    public SampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sample, parent, false);
        return new SampleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SampleViewHolder holder, int position) {
        Sample sample = getItem(position);
        if (sample != null){
            holder.tv_name.setText(sample.getName());
            if (sample.getImagePath() != null){
                //holder.iv_image.setImageBitmap(PhotoUtil.loadBitmapFromPath(sample.getImagePath()));
                if (PhotoUtil.loadBitmapFromPath(sample.getImagePath())==null){
                    holder.iv_image.setImageResource(R.drawable.icon_error);
                }
                else
                    Glide.with(holder.itemView.getContext())
                        .load(sample.getImagePath())
                        .placeholder(R.drawable.icon_loading)
                        .error(R.drawable.icon_error)
                        .into(holder.iv_image);
            }
            if (!sample.getDescription().isEmpty()) holder.tv_description.setText(sample.getDescription());
        }

        holder.tv_edit.setOnClickListener(v -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(position);
            }
        });

        holder.tv_delete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(position);
            }
        });
        holder.iv_image.setOnClickListener(v -> {
            if (onImageClickListener != null) {
                onImageClickListener.onImageClick(position);
            }
        });
        holder.tv_relationship.setOnClickListener(v -> {
            if (onRelationshipClickListener != null) {
                onRelationshipClickListener.onRelationshipClick(position);
            }
        });
    }

    static class SampleViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name,tv_description, tv_edit,tv_delete,tv_relationship;
        ImageView iv_image;

        SampleViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            iv_image = itemView.findViewById(R.id.iv_image);
            tv_description = itemView.findViewById(R.id.tv_description);
            tv_edit = itemView.findViewById(R.id.tv_edit);
            tv_delete = itemView.findViewById(R.id.tv_delete);
            tv_relationship = itemView.findViewById(R.id.tv_relationship);
        }
    }

    private static final DiffUtil.ItemCallback<Sample> DIFF_CALLBACK = new DiffUtil.ItemCallback<Sample>() {
        @Override
        public boolean areItemsTheSame(@NonNull Sample oldItem, @NonNull Sample newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Sample oldItem, @NonNull Sample newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public void submitList(@Nullable List<Sample> list) {
        super.submitList(list);
    }

    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public interface OnImageClickListener {
        void onImageClick(int position);
    }
    public interface OnRelationshipClickListener {
        void onRelationshipClick(int position);
    }
}
