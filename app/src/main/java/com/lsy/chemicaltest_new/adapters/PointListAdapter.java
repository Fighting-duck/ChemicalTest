package com.lsy.chemicaltest_new.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.Point;

public class PointListAdapter extends ListAdapter<Point,PointListAdapter.ViewHolder> {
    public interface OnDeletePointListener{
        void onDelete(int position);
    }
    private OnDeletePointListener mDeletePointListener;
    public void setOnDeletePointListener(OnDeletePointListener listener){
        mDeletePointListener = listener;
    }

    private static final DiffUtil.ItemCallback<Point> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Point>() {
                @Override
                public boolean areItemsTheSame(@NonNull Point oldItem, @NonNull Point newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Point oldItem, @NonNull Point newItem) {
                    return oldItem.equals(newItem);
                }
            };
    public PointListAdapter(){
        super(DIFF_CALLBACK);
    }


    //创建内部ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_sequence,tv_x, tv_y, tv_addTime;
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

    @NonNull
    @Override
    //调用时机：初始化时、滚动时、数据变化时、布局变化时
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pointlist_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    //调用时机：视图可见时、数据变化时、ViewHolder 被回收和重用时、布局变化时
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Point point = getItem(position);
        if (point!=null){
            holder.tv_sequence.setText(""+(position+1));
            holder.tv_x.setText(String.valueOf(point.getX_value()));
            holder.tv_y.setText(String.valueOf(point.getY_value()));
            /*String[] addTime = mPointList.get(position).getAdd_time().split(" ");
            if (addTime.length >= 2){
                holder.tv_addTime.setText(addTime[1]);
            }*/
            holder.tv_addTime.setText(point.getAdd_time());
        }
        //设置焦点变化监听器
        holder.iv_delete.setOnClickListener(v -> mDeletePointListener.onDelete(position));
    }
}
