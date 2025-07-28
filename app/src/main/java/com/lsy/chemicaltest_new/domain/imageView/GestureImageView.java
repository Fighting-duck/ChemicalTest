package com.lsy.chemicaltest_new.domain.imageView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
/**
 * 自定义ImageView，用于处理手势事件(单击，双击，长按)
 */
public class GestureImageView extends AppCompatImageView {

    private GestureDetector gestureDetector;
    private OnGestureListener gestureListener;

    public GestureImageView(Context context) {
        super(context);
        init(context);
    }

    public GestureImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GestureImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // 必须设置这些属性才能使手势检测正常工作
        setClickable(true);
        setFocusable(true);
        setLongClickable(true);

        // 初始化手势检测器
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void setOnGestureListener(OnGestureListener listener) {
        this.gestureListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 将触摸事件传递给GestureDetector
        boolean handled = gestureDetector.onTouchEvent(event);

        // 处理ACTION_UP事件以确保点击效果正常工作
        if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
        }

        return handled || super.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // 单击事件（确认不是双击的一部分）
            if (gestureListener != null) {
                gestureListener.onSingleTap(GestureImageView.this);
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // 双击事件
            if (gestureListener != null) {
                return gestureListener.onDoubleTap(GestureImageView.this);
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // 长按事件
            if (gestureListener != null) {
                gestureListener.onLongPress(GestureImageView.this);
            }
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // 必须返回true，这样后续事件才会被处理
            return true;
        }
    }

    // 手势监听器接口
    public interface OnGestureListener {
        void onSingleTap(View v);
        boolean onDoubleTap(View v);
        void onLongPress(View v);
    }
}