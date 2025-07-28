package com.lsy.chemicaltest_new.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.TestValue;

import java.util.ArrayList;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {
    private List<TestValue> mTestValueList = new ArrayList<>();

    //创建内部ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder{
        View view;
        TextView tv_sequenceNumber;
        TextView tv_value;
        TextView tv_unit;
        TextView tv_time;
        @SuppressLint("CutPasteId")
        public ViewHolder(View view){
            super(view);
            this.view = view;
            tv_sequenceNumber =  view.findViewById(R.id.tv_sequenceNumber);
            tv_value = view.findViewById(R.id.tv_value);
            tv_unit = view.findViewById(R.id.tv_unit);
            tv_time = view.findViewById(R.id.tv_time);
        }
    }

    @NonNull
    @Override
    //调用时机：初始化时、滚动时、数据变化时、布局变化时
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //定义一个构造
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        //创建布局
        View view;
        view = layoutInflater.inflate(R.layout.item_record,parent,false);
        //创建ViewHolder实例
        final ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    //调用时机：视图可见时、数据变化时、ViewHolder 被回收和重用时、布局变化时
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TestValue testValue = mTestValueList.get(position);
        int sequenceNumber = position+1;
        Float value = testValue.getValue();
        String unit = testValue.getUnit();
        String time = testValue.getTestTime();
        holder.tv_sequenceNumber.setText("No."+sequenceNumber);
        holder.tv_value.setText(String.valueOf(value));
        holder.tv_unit.setText(unit);
        holder.tv_time.setText(time);
    }

    @Override
    public int getItemCount() {
        if (mTestValueList == null)
            return 0;
        return mTestValueList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update(List<TestValue> testValues) {
        mTestValueList = testValues;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear(){
        mTestValueList.clear();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void remove(int position){
        mTestValueList.remove(position);
        notifyDataSetChanged();
    }
}

