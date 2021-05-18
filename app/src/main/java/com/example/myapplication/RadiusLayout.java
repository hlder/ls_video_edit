package com.example.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class RadiusLayout extends FrameLayout {
    public RadiusLayout(@NonNull Context context) {
        super(context);
    }

    public RadiusLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context,attrs,0);
    }

    public RadiusLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context,attrs,defStyleAttr);
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr){
        TypedArray typedArray=context.obtainStyledAttributes(attrs,R.styleable.RadiusLayout,defStyleAttr,0);


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

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(rect==null){
            rect=new RectF(0,0,getWidth(),getHeight());
            createPath();
        }
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
    private RectF rect;
    private Path path=new Path();


    private float leftTopRadius;
    private float leftBottomRaius;
    private float rightTopRadius;
    private float rightBottomRadius;


    @Override
    public void invalidate() {
        createPath();
        super.invalidate();
    }
    private void createPath(){
        if(rect!=null){
            path=new Path();
            float[] floats=new float[]{leftTopRadius,leftTopRadius,rightTopRadius,rightTopRadius,rightBottomRadius,rightBottomRadius,leftBottomRaius,leftBottomRaius};
            path.addRoundRect(rect,floats,Path.Direction.CW);
        }
    }

    private void setLeftTopRadius(int radius){
        this.leftTopRadius=radius;

        invalidate();
    }
    private void setLeftBottomRadius(int radius){
        this.leftBottomRaius=radius;
        invalidate();
    }
    private void setRightTopRadius(int radius){
        this.rightTopRadius=radius;
        invalidate();
    }
    private void setRightBottomRadius(int radius){
        this.rightBottomRadius=radius;
        invalidate();
    }

    private void setRadius(int radius){
        this.leftTopRadius=radius;
        this.leftBottomRaius=radius;
        this.rightTopRadius=radius;
        this.rightBottomRadius=radius;
        invalidate();
    }




}
