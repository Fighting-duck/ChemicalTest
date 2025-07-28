package com.lsy.chemicaltest_new.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.StandardCurve;


public class CurveAdapter extends ListAdapter<StandardCurve, CurveAdapter.ViewHolder> {

    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    public CurveAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_curve, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StandardCurve standardCurve = getItem(position);
        if (standardCurve != null){
            holder.tv_name.setText(standardCurve.getName());
            holder.tv_curveType.setText(standardCurve.getCurveType());
            holder.tv_formula.setText(standardCurve.getFormula().toString());
            holder.tv_corr.setText(String.valueOf(standardCurve.getCORR()));
            holder.tv_sample.setText(standardCurve.getSample().getName());
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
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name;
        TextView tv_curveType;
        TextView tv_formula;
        TextView tv_corr;
        TextView tv_sample;
        
        TextView tv_edit;
        TextView tv_delete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_curveName);
            tv_curveType = itemView.findViewById(R.id.tv_curveType);
            tv_formula = itemView.findViewById(R.id.tv_formula);
            tv_corr = itemView.findViewById(R.id.tv_corr);
            tv_sample = itemView.findViewById(R.id.tv_sample);
            tv_edit = itemView.findViewById(R.id.tv_edit);
            tv_delete = itemView.findViewById(R.id.tv_delete);
        }
    }

    private static final DiffUtil.ItemCallback<StandardCurve> DIFF_CALLBACK = new DiffUtil.ItemCallback<StandardCurve>() {
        @Override
        public boolean areItemsTheSame(@NonNull StandardCurve oldItem, @NonNull StandardCurve newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull StandardCurve oldItem, @NonNull StandardCurve newItem) {
            return oldItem.equals(newItem);
        }
    };

    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }
}
