package vn.techres.line.widget;

import android.content.Context;

import androidx.recyclerview.widget.LinearSmoothScroller;

import vn.techres.line.activity.TechResApplication;

public class CenterSmoothScroller extends LinearSmoothScroller {
    private int type = 0;
    public CenterSmoothScroller(Context context, int type) {
        super(context);
    }
    @Override
    public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
        if(type == 0)
        {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2) + TechResApplication.Companion.getHeightView() / 7;
        } else
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2) - TechResApplication.Companion.getHeightView() / 6;
    }
}
