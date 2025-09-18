package com.lsy.chemicaltest_new.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.models.StandardCurveViewModel;

import org.apache.commons.collections4.functors.IfClosure;

import java.util.List;

public class PointListAdapter extends RecyclerView.Adapter<PointListAdapter.ViewHolder> {
    private List<Point> mPointList;
    private Context mContext;
    //private final Integer mColumnNum;
    private StandardCurveViewModel mAddCurveViewModel = null ;

    //创建内部ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_sequence;
        TextView tv_x;
        TextView tv_y;
        TextView tv_addTime;
        ImageView iv_delete;
        @SuppressLint("CutPasteId")
        public ViewHolder(View view){
            super(view);
            tv_sequence =  view.findViewById(R.id.tv_sequence);
            tv_x =  view.findViewById(R.id.tv_x);
            tv_y = view.findViewById(R.id.tv_y);
            tv_addTime = view.findViewById(R.id.tv_addTime);
            iv_delete = view.findViewById(R.id.iv_delete);
        }
    }

    public PointListAdapter(Context context, StandardCurveViewModel viewModel){
        this.mContext = context;
        //this.mColumnNum = columnNum;
        this.mAddCurveViewModel = viewModel;
    }

    @NonNull
    @Override
    //调用时机：初始化时、滚动时、数据变化时、布局变化时
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //定义一个构造
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        //创建布局
        View view;
        view = layoutInflater.inflate(R.layout.pointlist_item,parent,false);
        //创建ViewHolder实例
        final ViewHolder holder = new ViewHolder(view);
        //设置焦点变化监听器
        holder.iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getItemCount() <= 2){
                    mAddCurveViewModel.setToast(mContext.getString(R.string.toast_curve_atLeastTwoPoints));
                    return;
                }
                // 获取当前点击项的位置
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
//                    // 从数据集中移除当前项
//                    mPointList.remove(currentPosition);
//                    // 通知 RecyclerView 数据集已更改
//                    notifyItemRemoved(currentPosition);
//                    notifyItemRangeChanged(currentPosition, mPointList.size());
//                    //更改Model中数据
                    if (mAddCurveViewModel!= null)
                        mAddCurveViewModel.removePoint(currentPosition);
                }
            }
        });
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    //调用时机：视图可见时、数据变化时、ViewHolder 被回收和重用时、布局变化时
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_sequence.setText(""+(position+1));
        Point point = mPointList.get(position);
        if (point!=null){
            holder.tv_x.setText(String.valueOf(mPointList.get(position).getX_value()));
            holder.tv_y.setText(String.valueOf(mPointList.get(position).getY_value()));
            /*String[] addTime = mPointList.get(position).getAdd_time().split(" ");
            if (addTime.length >= 2){
                holder.tv_addTime.setText(addTime[1]);
            }*/
            holder.tv_addTime.setText(mPointList.get(position).getAdd_time());
        }
    }

    @Override
    public int getItemCount() {
        if (mPointList == null)
            return 0;
        return mPointList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update(List<Point> newPointList){
        mPointList = newPointList;
        notifyDataSetChanged();
    }
}
