package com.lsy.chemicaltest_new.domain;

import android.graphics.Color;

public class RGB {
    private Integer red;
    private Integer green;
    private Integer blue;
    public RGB(Integer red, Integer green, Integer blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
    public RGB(RGB other){
        this.red = other.red;
        this.green = other.green;
        this.blue = other.blue;
    }
    public Integer getRed() {
        return red;
    }
    public Integer getGreen() {
        return green;
    }
    public Integer getBlue() {
        return blue;
    }
    public void setRed(Integer red) {
        this.red = red;
    }
    public void setGreen(Integer green) {
        this.green = green;
    }
    public void setBlue(Integer blue) {
        this.blue = blue;
    }
    public int toColor() {
        return Color.rgb(red, green, blue);
    }
    @Override
    public String toString() {
        return "RGB{" +
                "red=" + red +
                ", green=" + green +
                ", blue=" + blue +
                '}';
    }
    public String toHex() {
        return String.format("#%02x%02x%02x", red, green, blue);
    }
    public String toRGBString() {
        return "rgb(" + red + "," + green + "," + blue + ")";
    }
    public static RGB fromHex(String hexColor) {
        int colorInt = Color.parseColor(hexColor);
        return new RGB(Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt));
    }
    public static RGB fromRGBString(String rgbString) {
        String[] rgbParts = rgbString.substring(4, rgbString.length() - 1).split(",");
        return new RGB(Integer.parseInt(rgbParts[0]), Integer.parseInt(rgbParts[1]), Integer.parseInt(rgbParts[2]));
    }
    public static RGB fromColor(int color) {
        return new RGB(Color.red(color), Color.green(color), Color.blue(color));
    }
}
