package com.lsy.chemicaltest_new.adapters;

import static com.lsy.chemicaltest_new.utils.DynamicStringUtils.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.CorrectCurveItem;
import com.lsy.chemicaltest_new.fragments.ConnectMultimeterFragment;
import com.lsy.chemicaltest_new.utils.NumberUtils;

public class CorrectCurveAdapter extends ListAdapter<CorrectCurveItem, CorrectCurveAdapter.ViewHolder> {
    private static Context mContext;
    public interface OnTestClickListener {
        void onTestClick(int position);
    }
    public interface OnLongClickListener {
        void onLongClick(int position);
    }
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }
    private OnTestClickListener listener;
    private OnLongClickListener longClickListener;
    private OnDeleteClickListener deleteClickListener;
    public void setOnTestClickListener(OnTestClickListener listener){
        this.listener = listener;
    }
    public void setOnLongClickListener(OnLongClickListener listener){
        this.longClickListener = listener;
    }
    public void setOnDeleteClickListener(OnDeleteClickListener listener){
        this.deleteClickListener = listener;
    }

    private static final DiffUtil.ItemCallback<CorrectCurveItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CorrectCurveItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull CorrectCurveItem oldItem, @NonNull CorrectCurveItem newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(@NonNull CorrectCurveItem oldItem, @NonNull CorrectCurveItem newItem) {
                    return oldItem.equals(newItem);
                }
            };
    public CorrectCurveAdapter(Context mContext)
    {
        super(DIFF_CALLBACK);
        this.mContext = mContext;
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tv_sequenceNumber,tv_CO,tv_current,tv_measuredCurrent,tv_delete, tv_multiTest;
        ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tv_sequenceNumber = itemView.findViewById(R.id.tv_sequenceNumber);
            tv_CO = itemView.findViewById(R.id.tv_CO);
            tv_current = itemView.findViewById(R.id.tv_current);
            tv_measuredCurrent = itemView.findViewById(R.id.tv_TestCurrent);
            tv_delete = itemView.findViewById(R.id.tv_delete);
            tv_multiTest = itemView.findViewById(R.id.tv_MultimeterTest);
        }
        void bind(CorrectCurveItem item, int position)
        {
            tv_sequenceNumber.setText(String.valueOf(position+1));
            tv_CO.setText(String.valueOf(NumberUtils.roundCurve_lgX_avgY(item.getPoint().getX_value())));
            tv_current.setText(String.valueOf(NumberUtils.roundCurve_lgX_avgY(item.getPoint().getY_value())));
            Float corrected_y = item.getCorrected_y();
            if (corrected_y == null){
                tv_measuredCurrent.setText(getString(R.string.default_no));
                tv_delete.setVisibility(View.GONE);
            }
            else{
                tv_measuredCurrent.setText(String.valueOf(NumberUtils.roundCurve_lgX_avgY(item.getCorrected_y())));
                tv_delete.setVisibility(View.VISIBLE);
            }
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_correct_curve, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CorrectCurveItem item = getItem(position);
        if (item == null) return;
        holder.bind(item,position);
        // 设置点击监听器
        setClickListeners(holder, position);
    }
    private void setClickListeners(ViewHolder holder, int position) {
        // 单选按钮点击
        holder.tv_multiTest.setOnClickListener(v -> {
            listener.onTestClick(position);
        });
        // 长按监听器
        holder.itemView.setOnLongClickListener(v -> {
            holder.itemView.setBackgroundColor(mContext.getColor(R.color.radio_button_checked));
            longClickListener.onLongClick(position);
            return true;
        });
        // 删除按钮点击
        holder.tv_delete.setOnClickListener(v -> {
            deleteClickListener.onDeleteClick(position);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void notifyAllItemRangeChanged() {
        notifyItemRangeChanged(0, getItemCount());
    }
}
