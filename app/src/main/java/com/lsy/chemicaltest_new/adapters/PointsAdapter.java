package com.lsy.chemicaltest_new.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.models.StandardCurveViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.ViewHolder> {
    List<Double> mValueList;
    Context mContext;
    Integer mColumnNum;
    Double mDefaultValue_y;
    StandardCurveViewModel mViewModel = null ;

    //创建内部ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_top;
        TextView edt_value;


        @SuppressLint("CutPasteId")
        public ViewHolder(View view){
            super(view);
            tv_top =  view.findViewById(R.id.tv_top);
            edt_value = view.findViewById(R.id.edt_value);
        }
    }

    public PointsAdapter(Context context, StandardCurveViewModel viewModel, Integer columnNum){
        this.mContext = context;
        this.mColumnNum = columnNum;
        this.mViewModel = viewModel;
        this.mDefaultValue_y = Double.valueOf(mContext.getString(R.string.default_float_number_0));
        int defaultNum = Integer.parseInt(mContext.getString(R.string.default_point_number));
        mValueList = new ArrayList<>(Collections.nCopies(2*(defaultNum+1), 0.0d));
    }

    @NonNull
    @Override
    //调用时机：初始化时、滚动时、数据变化时、布局变化时
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //定义一个构造
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        //创建布局
        View view;
        view = layoutInflater.inflate(R.layout.point_item,parent,false);
        //创建ViewHolder实例
        final ViewHolder holder = new ViewHolder(view);
        //设置焦点变化监听器
        holder.edt_value.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String str_value = ((EditText) v).getText().toString();
                if (str_value.isEmpty() || str_value.equals(".")) {
                    mViewModel.setToast(mContext.getString(R.string.toast_curve_inputEmpty));
                    ((EditText) v).setText(String.valueOf(mDefaultValue_y));
                    mValueList.set(holder.getLayoutPosition(), mDefaultValue_y);
                    calculate_x_y(mValueList);
                }
            }
        });
        //设置文本变化监听器
        holder.edt_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().trim();
                try {
                    Double value = Double.valueOf(input);
                    // 处理有效的浮点数
                    mValueList.set(holder.getLayoutPosition(),value);
                    calculate_x_y(mValueList);
                } catch (NumberFormatException e) {
                    // 处理无效输入
                    e.printStackTrace();
                }
            }
        });
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    //调用时机：视图可见时、数据变化时、ViewHolder 被回收和重用时、布局变化时
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position% mColumnNum == 0) { //判断是不是头行 =0为头行
            holder.tv_top.setVisibility(View.VISIBLE);
            holder.edt_value.setVisibility(View.GONE);
            if (position==0)
                holder.tv_top.setText("X");
            else
                holder.tv_top.setText("Y"+position/2);
        } else {
            holder.tv_top.setVisibility(View.GONE);
            holder.edt_value.setVisibility(View.VISIBLE);
            holder.edt_value.setText(String.valueOf(mValueList.get(position)));
        }
    }

    @Override
    public int getItemCount() {
        if (mValueList == null)
            return 0;
        return mValueList.size();
    }

    //计算平均值x或y,显示
    private void calculate_x_y(List<Double> valueList){
        //计算平均值y
        Double y_total = 0.0d;
        for(int i=3; i<valueList.size(); i++) {
            if (i% mColumnNum != 0)  //判断是不是头行 =0为头行
                y_total += valueList.get(i);
        }
        Double y_average = y_total/(valueList.size()/2-1);
        //显示x和y
        if (mViewModel != null) {
            mViewModel.set_point_x(valueList.get(1));
            mViewModel.set_point_y(y_average);
        }

    }
    //改变Y值数量
    @SuppressLint("NotifyDataSetChanged")
    public void alter_YNum(Integer yNum){
        int currYNum = mValueList.size() / mColumnNum - 1;//当前Y值数量
        if (yNum>=1){
            if (yNum<currYNum){
                while (yNum !=currYNum){
                    mValueList.remove(mValueList.size()-1);
                    currYNum = (mValueList.size()+1) / mColumnNum - 1;
                }
            }
            else{
                while (yNum !=currYNum){
                    mValueList.add(mDefaultValue_y);
                    currYNum = mValueList.size() / mColumnNum - 1;
                }
            }
            notifyDataSetChanged();
        }

    }

    //改变指定位置的Y值
    @SuppressLint("NotifyDataSetChanged")
    public void alter_YValue(Integer position, Double value){
        if (position>=0 && position<mValueList.size()) {
            mValueList.set(position, value);
            notifyDataSetChanged();
        }
    }
}
