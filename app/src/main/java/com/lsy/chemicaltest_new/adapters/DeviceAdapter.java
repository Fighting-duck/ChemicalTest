package com.lsy.chemicaltest_new.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clj.fastble.data.BleDevice;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.utils.BleUtil;

import java.util.ArrayList;
import java.util.List;
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    private List<BleDevice> mBleDeviceList;
    private BleUtil mBleUtil;

    //创建内部ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder{
        View view;
        TextView tv_BleDeviceName;
        TextView tv_address;
        TextView tv_rssi;


        @SuppressLint("CutPasteId")
        public ViewHolder(View view){
            super(view);
            this.view = view;
            tv_BleDeviceName =  view.findViewById(R.id.tv_name);
            tv_address = view.findViewById(R.id.tv_address);
            tv_rssi = view.findViewById(R.id.tv_rssi);
        }
    }

    public DeviceAdapter(BleUtil bleUtil){
        mBleDeviceList  = new ArrayList<BleDevice>();
        this.mBleUtil = bleUtil;
    }

    @NonNull
    @Override
    //调用时机：初始化时、滚动时、数据变化时、布局变化时
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //定义一个构造
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        //创建布局
        View view;
        view = layoutInflater.inflate(R.layout.item_device,parent,false);
        //创建ViewHolder实例
        final ViewHolder holder = new ViewHolder(view);

        holder.view.setOnClickListener(view1 -> {
            BleDevice device = (BleDevice) view1.getTag(R.id.my_tag);
            //连接设备
            mBleUtil.connectBle(device);
        });

        return holder;
    }

    @Override
    //调用时机：视图可见时、数据变化时、ViewHolder 被回收和重用时、布局变化时
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BleDevice device = mBleDeviceList.get(position);
        //使用 setTag() 和 getTag() 方法来存储和检索数据
        holder.view.setTag(R.id.my_tag, device);
        holder.tv_BleDeviceName.setText(device.getName());
        holder.tv_address.setText(device.getMac());
        holder.tv_rssi.setText(String.valueOf(device.getRssi()));
    }

    @Override
    public int getItemCount() {
        if (mBleDeviceList == null)
            return 0;
        return mBleDeviceList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update(List<BleDevice> newBleDeviceList){
        mBleDeviceList = newBleDeviceList;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear(){
        mBleDeviceList.clear();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void remove(int position){
        mBleDeviceList.remove(position);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void add(BleDevice BleDevice){
        mBleDeviceList.add(BleDevice);
        notifyDataSetChanged();
    }


}

