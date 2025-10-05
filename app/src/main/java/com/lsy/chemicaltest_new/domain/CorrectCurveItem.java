package com.lsy.chemicaltest_new.domain;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CorrectCurveItem {
    private Point point = null;
    private Float corrected_y = null;

    private String add_time = null;

    public CorrectCurveItem(){

    }
    public CorrectCurveItem(Point point, Float corrected_y) {
        this.point = point;
        this.corrected_y = corrected_y;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public Float getCorrected_y() {
        return corrected_y;
    }

    public void setCorrected_y(Float corrected_y) {
        this.corrected_y = corrected_y;
    }

    public String getAdd_time() {
        return add_time;
    }

    public void setAdd_time(String add_time) {
        this.add_time = add_time;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;

        if (obj.getClass() != this.getClass()) return false;
        else {
            CorrectCurveItem curveItem = (CorrectCurveItem) obj;
            return curveItem.getPoint().equals(this.point) &&
                    curveItem.getCorrected_y().equals(this.corrected_y) &&
                    curveItem.getAdd_time().equals(this.add_time);
        }
    }

    public static List<Point> getCorrectedPointList(List<CorrectCurveItem> itemList){
        List<Point> pointList = new ArrayList<>();
        for (CorrectCurveItem item : itemList) {
            if (item != null){
                if (item.getCorrected_y() != null){
                    pointList.add(new Point(item.getPoint().getX_value(), item.getCorrected_y(), item.getAdd_time()));
                }
                else {
                    pointList.add(item.getPoint());
                }
            }
        }
        return pointList;
    }
}
