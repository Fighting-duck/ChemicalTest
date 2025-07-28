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

public class TestValueAdapter extends RecyclerView.Adapter<TestValueAdapter.ViewHolder> {
    private static final String TAG = "TestValueAdapter";
    private List<TestValue> mTestValueList= new ArrayList<TestValue>();

    public TestValueAdapter(){
    }
    //创建内部ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder{
        View view;
        TextView tv_num;
        TextView tv_testValue;
        TextView tv_time;

        @SuppressLint("CutPasteId")
        public ViewHolder(@NonNull View view){
            super(view);
            this.view = view;
            tv_num =  view.findViewById(R.id.tv_num);
            tv_testValue = view.findViewById(R.id.tv_testValue);
            tv_time = view.findViewById(R.id.tv_testTime);
        }
    }

    @NonNull
    @Override
    //调用时机：初始化时、滚动时、数据变化时、布局变化时
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //定义一个构造
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        //创建布局
        View view  = layoutInflater.inflate(R.layout.item_testvalue,parent,false);
        //创建ViewHolder实例
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    //调用时机：视图可见时、数据变化时、ViewHolder 被回收和重用时、布局变化时
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TestValue testValue = mTestValueList.get(position);
        if (testValue == null) return;
        holder.tv_num.setText(String.valueOf(position+1));
        holder.tv_testValue.setText(String.valueOf(testValue.getValue()));
        holder.tv_time.setText(testValue.getTestTime());
    }

    @Override
    public int getItemCount() {
        if (mTestValueList == null)
            return 0;
        return mTestValueList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update(List<TestValue> TestValues) {
        mTestValueList = TestValues;
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

