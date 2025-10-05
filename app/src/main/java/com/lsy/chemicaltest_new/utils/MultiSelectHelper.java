package com.lsy.chemicaltest_new.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

// 多选模式辅助类
public class MultiSelectHelper {
    //内部接口
    public interface OnMultiSelectListener {
        void onMultiSelectChanged(boolean isMultiSelectMode, int selectedCount);
    }
    private final CopyOnWriteArraySet<Integer> selectedPositions = new CopyOnWriteArraySet<>();//多选模式下选中项
    private OnMultiSelectListener multiSelectListener;//多选模式监听器
    private boolean isMultiSelectMode = false;//多选模式

    public MultiSelectHelper() {
    }
    // 将用户选择的项加入多选模式
    public void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        updateMultiSelectMode();
    }
    // 清空多选模式
    public void clearSelection() {
        selectedPositions.clear();
        updateMultiSelectMode();
    }
    // 更新多选模式
    public void updateMultiSelectMode() {
        boolean newMode = !selectedPositions.isEmpty();
        // 只有状态变化时才通知
        if (isMultiSelectMode != newMode) {
            isMultiSelectMode = newMode;
            notifyListener();
        }
    }
    // 通知监听器
    public void notifyListener() {
        if (multiSelectListener != null) {
            multiSelectListener.onMultiSelectChanged(isMultiSelectMode, selectedPositions.size());
        }
    }
    public void setMultiSelectListener(OnMultiSelectListener listener) {
        multiSelectListener = listener;
    }

    public OnMultiSelectListener getMultiSelectListener() {
        return multiSelectListener;
    }

    public boolean  isMultiSelectMode() {
        return isMultiSelectMode;
    }

    public List<Integer> getSelectedPositions() {
        return new ArrayList<>(selectedPositions);
    }
}
