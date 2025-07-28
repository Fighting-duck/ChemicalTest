package com.lsy.chemicaltest_new.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class LimitedArrayAdapter<T> extends ArrayAdapter<T> {
    private int maxItems;

    public LimitedArrayAdapter(Context context, int resource, List<T> objects, int maxItems) {
        super(context, resource, objects);
        this.maxItems = maxItems;
    }

    @Override
    public int getCount() {
        // 返回最大显示条目数和实际条目数中的较小值
        return Math.min(super.getCount(), maxItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        view.setText(getItem(position).toString());
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        view.setText(getItem(position).toString());
        return view;
    }
}