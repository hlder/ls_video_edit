package com.example.myapplication.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.example.myapplication.R;

public class RadiusHandler {
    RadiusHandler(View view){
        this.view=view;
    }

    private View view;

    private float leftTopRadius;
    private float leftBottomRaius;
    private float rightTopRadius;
    private float rightBottomRadius;


    private RectF rect;
    private Path path=new Path();


    private void createPath(){
        if(rect!=null){
            path=new Path();
            float[] floats=new float[]{leftTopRadius,leftTopRadius,rightTopRadius,rightTopRadius,rightBottomRadius,rightBottomRadius,leftBottomRaius,leftBottomRaius};
            path.addRoundRect(rect,floats,Path.Direction.CW);
        }
    }

    protected void onDraw(Canvas canvas) {
        if(rect==null){
            rect=new RectF(0,0,view.getWidth(),view.getHeight());
            createPath();
        }
        canvas.clipPath(path);

    }
    void initAttrs(Context context, AttributeSet attrs, int defStyleAttr){
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.RadiusLayout,defStyleAttr,0);


        float radius=typedArray.getDimension(R.styleable.RadiusLayout_radius,-1);
        if(radius!=-1){
            leftTopRadius = radius;
            leftBottomRaius = radius;
            rightTopRadius = radius;
            rightBottomRadius = radius;
        }else{
            leftTopRadius = typedArray.getDimension(R.styleable.RadiusLayout_leftTopRadius,0);
            leftBottomRaius = typedArray.getDimension(R.styleable.RadiusLayout_leftBottomRadius,0);
            rightTopRadius=typedArray.getDimension(R.styleable.RadiusLayout_rightTopRadius,0);
            rightBottomRadius=typedArray.getDimension(R.styleable.RadiusLayout_rightBottomRadius,0);
        }

        typedArray.recycle();

        view.setBackgroundColor(Color.TRANSPARENT);

    }



     void setLeftTopRadius(int radius){
        this.leftTopRadius=radius;
        createPath();

        view.invalidate();
    }
     void setLeftBottomRadius(int radius){
        this.leftBottomRaius=radius;
        createPath();
        view.invalidate();
    }
     void setRightTopRadius(int radius){
        this.rightTopRadius=radius;
        createPath();
        view.invalidate();
    }
     void setRightBottomRadius(int radius){
        this.rightBottomRadius=radius;
        createPath();
        view.invalidate();
    }

     void setRadius(int radius){
        this.leftTopRadius=radius;
        this.leftBottomRaius=radius;
        this.rightTopRadius=radius;
        this.rightBottomRadius=radius;
        createPath();
        view.invalidate();
    }
}
