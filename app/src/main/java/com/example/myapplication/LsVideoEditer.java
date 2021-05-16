package com.example.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Matrix.ScaleToFit.CENTER;
import static android.graphics.Matrix.ScaleToFit.FILL;

public class LsVideoEditer extends View {
    public LsVideoEditer(Context context) {
        super(context);
        init();
    }

    public LsVideoEditer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);



        init();
    }

    public LsVideoEditer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void initAttrs(Context context, AttributeSet attrs){
        TypedArray typedArray=context.obtainStyledAttributes(attrs,R.styleable.lsVideoEditer);

        float leftTopIconWidth=typedArray.getDimension(R.styleable.lsVideoEditer_leftTopIconWidth,20);
        float leftTopIconHeight=typedArray.getDimension(R.styleable.lsVideoEditer_leftTopIconHeight,20);

        float leftBottomIconWidth=typedArray.getDimension(R.styleable.lsVideoEditer_leftBottomIconWidth,20);
        float leftBottomIconHeight=typedArray.getDimension(R.styleable.lsVideoEditer_leftBottomIconHeight,20);

        float rightTopIconWidth=typedArray.getDimension(R.styleable.lsVideoEditer_rightTopIconWidth,20);
        float rightTopIconHeight=typedArray.getDimension(R.styleable.lsVideoEditer_rightTopIconHeight,20);

        float rightBottomIconWidth=typedArray.getDimension(R.styleable.lsVideoEditer_rightBottomIconWidth,20);
        float rightBottomIconHeight=typedArray.getDimension(R.styleable.lsVideoEditer_rightBottomIconHeight,20);


        int leftTopIconIconId=typedArray.getResourceId(R.styleable.lsVideoEditer_leftTopIcon,0);

        int leftBottomIconId=typedArray.getResourceId(R.styleable.lsVideoEditer_leftBottomIcon,0);

        int rightTopIconId=typedArray.getResourceId(R.styleable.lsVideoEditer_rightTopIcon,0);

        int rightBottomIconId=typedArray.getResourceId(R.styleable.lsVideoEditer_rightBottomIcon,0);


        if(leftTopIconIconId!=0){
            leftTopIcon=BitmapFactory.decodeResource(getResources(),leftTopIconIconId);
        }
        if(leftBottomIconId!=0){
            leftBottomIcon=BitmapFactory.decodeResource(getResources(),leftBottomIconId);
        }

    }


    private void init(){


        resBitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.icon_test);

        list.add(Item.createItem(0,0,resBitmap.getWidth(),resBitmap.getHeight()));
        list.add(Item.createItem(200,200,200+resBitmap.getWidth(),200+resBitmap.getHeight()));
    }

    private Bitmap resBitmap;//可以拖动的bitmap


    private final int hotSize=100;//热点区域的大小

    private float touchX=100;//touch的x轴
    private float touchY=100;//touch的y轴的位置


    private Bitmap leftTopIcon;//左上角的图标
    private Bitmap rightTopIcon;//右上角的图标
    private Bitmap leftBottomIcon;//左下角的图标
    private Bitmap rightBottomIcon;//右下角的图标


    private int vWidth;
    private int vHeight;



    //存放bitmap的信息
    private List<Item> list=new ArrayList<>();




    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if(vWidth==0) vWidth=getWidth();
        if(vHeight==0) vHeight=getHeight();

        Paint paint=new Paint();
        paint.setTextSize(50);


        if(resBitmap!=null){
            for(Item item:list){
                Matrix matrix = new Matrix();

                float tw=item.rectF.right-item.rectF.left;
                float th=item.rectF.bottom-item.rectF.top;

                float blX=tw/resBitmap.getWidth();
                float blY=th/resBitmap.getHeight();


                if(item.isFlip()){
                    matrix.postScale(-1,1,resBitmap.getWidth()/2,resBitmap.getHeight()/2);
                }else{
                    matrix.postScale(blX,blY);
                }


                matrix.postTranslate(item.rectF.left,item.rectF.top);
                canvas.drawBitmap(resBitmap,matrix,paint);
            }
        }

    }






    //手指选中的item
    private Item selectedItem;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        touchX=event.getX();
        touchY=event.getY();

        int action=event.getAction();

        if(action==MotionEvent.ACTION_DOWN){
            selectedItem=null;
            boolean isSelected=false;
            for(int i=list.size()-1;i>=0;i--){
                Item item=list.get(i);
                RectF rectF=item.getRectF();
                if(!isSelected&&touchX>rectF.left&&touchX<rectF.right&&touchY>rectF.top&&touchY<rectF.bottom){
                    //表明touch的点已经在该图片里面了，下面判断是否是在左下角和右下角，当在左下角和右下角时候，不能被拖动
                    item.setSelected(true);
                    isSelected=true;

                    if(touchX>(rectF.right-hotSize)&&touchY>(rectF.bottom-hotSize)){
                        item.setType(1);//缩放
                    }else if(touchX<(rectF.left+hotSize)&&touchY>(rectF.bottom-hotSize)){
                        item.setType(2);//翻转
                    }else if(touchX>(rectF.right-hotSize)&&touchY<(rectF.top+hotSize)){
                        item.setType(3);//删除
                    }else{
                        item.setType(0);//移动
                    }
                    //表示按住了右下角
                    item.setLeftDistance(touchX-rectF.left);
                    item.setTopDistance(touchY-rectF.top);

                    selectedItem=item;
                }else{
                    item.setSelected(false);
                }
            }
        }else if(action==MotionEvent.ACTION_UP){
        }else if(action==MotionEvent.ACTION_CANCEL){
        }else if(MotionEvent.ACTION_MOVE==action){
            if(selectedItem!=null){
                if(selectedItem.getType()==0){//移动
                    RectF rectF=selectedItem.getRectF();
                    float w=rectF.right-rectF.left;
                    float h=rectF.bottom-rectF.top;
                    rectF.left=touchX-selectedItem.leftDistance;
                    rectF.top=touchY-selectedItem.topDistance;
                    rectF.right=rectF.left+w;
                    rectF.bottom=rectF.top+h;
                    invalidate();
                }else if(selectedItem.getType()==1){//缩放
                    RectF rectF=selectedItem.getRectF();
                    float temWidth=rectF.right-rectF.left;
                    float temHeight=rectF.bottom-rectF.top;

                    float temx=touchX-rectF.left;

                    float temRight=rectF.left+temx;
                    float temBottom=rectF.top+temx*temHeight/temWidth;
                    if(temRight>(rectF.left+hotSize*2)&&temBottom>(rectF.top+hotSize*2)){
                        rectF.right=rectF.left+temx;
                        rectF.bottom=rectF.top+temx*temHeight/temWidth;
                        invalidate();
                    }

                }
            }
        }

        return true;
    }







    public void setRightTopIcon(int rightTopIcon) {
        this.rightTopIcon=BitmapFactory.decodeResource(getResources(),rightTopIcon);
    }

    public void setRightTopIcon(Bitmap rightTopIcon) {
        this.rightTopIcon = rightTopIcon;
    }

    public void setRightBottomIcon(int rightBottomIcon) {
        this.rightBottomIcon = BitmapFactory.decodeResource(getResources(),rightBottomIcon);
    }

    public void setRightBottomIcon(Bitmap rightBottomIcon) {
        this.rightBottomIcon = rightBottomIcon;
    }

    public void setLeftTopIcon(int leftTopIcon) {
        this.leftTopIcon = BitmapFactory.decodeResource(getResources(),leftTopIcon);
    }

    public void setLeftTopIcon(Bitmap leftTopIcon) {
        this.leftTopIcon = leftTopIcon;
    }

    public void setLeftBottomIcon(int leftBottomIcon) {
        this.leftBottomIcon = BitmapFactory.decodeResource(getResources(),leftBottomIcon);
    }

    public void setLeftBottomIcon(Bitmap leftBottomIcon) {
        this.leftBottomIcon = leftBottomIcon;
    }

    public void setResBitmap(Bitmap resBitmap) {
        this.resBitmap = resBitmap;
    }



    static class Item{
        private boolean isSelected;//是否被选中,当touchdown的时候进行判断

        private boolean isFlip=false;//是否翻转

        private int type=0;//0表示move，1表示右下角缩放，2表示翻转按钮,3表示右上角删除按钮

        private RectF rectF;

        private float leftDistance;//计算方式为:x-left
        private float topDistance;//计算方式为：y-top




        static Item createItem(float l,float t,float r,float b){
            Item item=new Item();

            item.setRectF(new RectF(l,t,r,b));
            return item;
        }


        public void setLeftDistance(float leftDistance) {
            this.leftDistance = leftDistance;
        }

        public void setTopDistance(float topDistance) {
            this.topDistance = topDistance;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public void setFlip(boolean flip) {
            isFlip = flip;
        }

        public boolean isFlip() {
            return isFlip;
        }

        public float getTopDistance() {
            return topDistance;
        }

        public float getLeftDistance() {
            return leftDistance;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setRectF(RectF rectF) {
            this.rectF = rectF;
        }
        public RectF getRectF() {
            return rectF;
        }
    }

}
