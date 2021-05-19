package com.example.myapplication.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class RadiusLinearLayout extends LinearLayout {
    private final RadiusHandler radiusHandler;
    public RadiusLinearLayout(Context context) {
        super(context);
        radiusHandler=new RadiusHandler(this);
    }

    public RadiusLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        radiusHandler=new RadiusHandler(this);
        radiusHandler.initAttrs(context,attrs,0);
    }

    public RadiusLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        radiusHandler=new RadiusHandler(this);
        radiusHandler.initAttrs(context,attrs,defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        radiusHandler.onDraw(canvas);
        super.onDraw(canvas);
    }
    private void setLeftTopRadius(int radius){
        radiusHandler.setLeftTopRadius(radius);
    }
    private void setLeftBottomRadius(int radius){
        radiusHandler.setLeftBottomRadius(radius);
    }
    private void setRightTopRadius(int radius){
        radiusHandler.setRightTopRadius(radius);
    }
    private void setRightBottomRadius(int radius){
        radiusHandler.setRightBottomRadius(radius);
    }
    private void setRadius(int radius){
        radiusHandler.setRadius(radius);
    }

}
