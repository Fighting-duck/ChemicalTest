package com.lsy.chemicaltest_new.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;


import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.utils.MultiSelectHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurveAdapter  extends ListAdapter<StandardCurve, CurveAdapter.ViewHolder> {

    // 接口定义
    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    private final MultiSelectHelper multiSelectHelper = new MultiSelectHelper();//多选模式辅助类
    private OnEditClickListener editClickListener;//编辑监听器
    private Context mContext;

    // 设置监听器
    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editClickListener = listener;
    }

    public void setOnMultiSelectListener(MultiSelectHelper.OnMultiSelectListener listener) {
        multiSelectHelper.setMultiSelectListener(listener);
    }

    private static final DiffUtil.ItemCallback<StandardCurve> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<StandardCurve>() {
                @Override
                public boolean areItemsTheSame(@NonNull StandardCurve oldItem, @NonNull StandardCurve newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(@NonNull StandardCurve oldItem, @NonNull StandardCurve newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public CurveAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.mContext = context;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tv_name, tv_curveType, tv_formula, tv_corr, tv_sample;
        final RadioButton radio_btn;
        final LinearLayout ll_main;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ll_main = itemView.findViewById(R.id.ll_main);
            tv_name = itemView.findViewById(R.id.tv_curveName);
            tv_curveType = itemView.findViewById(R.id.tv_curveType);
            tv_formula = itemView.findViewById(R.id.tv_formula);
            tv_corr = itemView.findViewById(R.id.tv_corr);
            tv_sample = itemView.findViewById(R.id.tv_sample);
            radio_btn = itemView.findViewById(R.id.radio_btn);
        }

        void bind(StandardCurve item) {
            tv_name.setText(item.getName());
            tv_curveType.setText(item.getCurveType());
            tv_formula.setText(item.getFormula().toString());
            tv_corr.setText(String.valueOf(item.getCORR()+"%"));
            tv_sample.setText(item.getSample().getName());
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        void updateSelectionStyle(boolean isMultiSelectMode, boolean isSelected, Context  context) {
            // 更新背景样式
            if (isMultiSelectMode && isSelected){
                ll_main.setBackground(context.getDrawable(R.drawable.item_selected_background));
            }
            else {
                ll_main.setBackground(context.getDrawable(R.drawable.item_unselected_background));
            }
            // 缩放动画
            float scale = isMultiSelectMode && isSelected ? 0.94f : 1.0f;
            ll_main.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(200) // 动画时长（毫秒）
                    .start();
            radio_btn.setChecked(isMultiSelectMode && isSelected);// 多选模式下单选按钮可见
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_curve, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StandardCurve item = getItem(position);
        if (item == null) return;

        holder.bind(item);
        // 更新多选模式样式
        holder.updateSelectionStyle(multiSelectHelper.isMultiSelectMode(),
                multiSelectHelper.getSelectedPositions().contains(position),mContext);
        // 根据多选模式控制单选按钮显隐
        holder.radio_btn.setVisibility(
                multiSelectHelper.isMultiSelectMode() ? View.VISIBLE : View.GONE
        );
        // 设置点击监听器
        setClickListeners(holder, position);
    }

    private void setClickListeners(ViewHolder holder, int position) {
        // 长按开启多选模式
        holder.ll_main.setOnLongClickListener(v -> {
            selectItem(position);
            return true;
        });
        // 单选按钮点击
        holder.radio_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (multiSelectHelper.isMultiSelectMode()) {
                    selectItem(position);
                }
            }
        });
        // 点击进入编辑模式
        holder.ll_main.setOnClickListener(v -> {
            if (multiSelectHelper.isMultiSelectMode()) {
                holder.radio_btn.setChecked(true);
                selectItem(position);
            }
            else {
                if (editClickListener != null) editClickListener.onEditClick(position);
            }
        });
    }

    private void selectItem(int position) {
        // 1. 选中当前项
        multiSelectHelper.toggleSelection(position);
        // 2. 通知刷新所有可见项（使单选按钮显示）
        notifyItemRangeChanged(0, getItemCount());
    }

    /**
     * 退出多选模式
     */
    public void exitMultiSelectMode() {
        multiSelectHelper.clearSelection();
        notifyDataSetChanged();
    }
    /**
     * 判断是否处于多选模式
     */
    public boolean isMultiSelectMode() {
        return multiSelectHelper.isMultiSelectMode();
    }
    /**
     * 获取选中的项
     */
    public List<StandardCurve> getSelectedItems() {
        List<StandardCurve> selectedItems = new ArrayList<>();
        for (Integer pos : multiSelectHelper.getSelectedPositions()) {
            selectedItems.add(getItem(pos));
        }
        return selectedItems;
    }
    /**
     * 删除选中项
     */
    public void deleteSelectedItems() {
        List<StandardCurve> newList = new ArrayList<>(getCurrentList());// 创建一个新的列表,所有标准曲线
        List<Integer> sortedPositions = new ArrayList<>(multiSelectHelper.getSelectedPositions());
        Collections.sort(sortedPositions, Collections.reverseOrder());// 倒序排序
        for (int pos : sortedPositions) {
            if (pos >= 0 && pos < newList.size()) {
                newList.remove(pos);
            }
        }
        submitList(newList);
        multiSelectHelper.clearSelection();
    }
}
