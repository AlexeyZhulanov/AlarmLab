package com.example.alarm;

public class ColorThemeMenuItem {
    public int mainColorRes;
    public int secondColorRes;
    public int themeNumber;

    public ColorThemeMenuItem(int mainColorRes, int secondColorRes, int themeNumber) {
        this.mainColorRes = mainColorRes;
        this.secondColorRes = secondColorRes;
        this.themeNumber = themeNumber;
    }
    public int getThemeNumber() {
        return themeNumber;
    }
}