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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 自定义编辑view，可以自由添加，缩放item，翻转，拖动等操作
 * hld
 */
public class LsVideoEditer extends View {
    public LsVideoEditer(Context context) {
        super(context);
        init();
    }
    public LsVideoEditer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context,attrs,0);
        init();
    }
    public LsVideoEditer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context,attrs,defStyleAttr);
        init();
    }
    private final float defSize=50;

    private float leftTopIconWidth=defSize;
    private float leftTopIconHeight=defSize;
    private float leftBottomIconWidth=defSize;
    private float leftBottomIconHeight=defSize;
    private float rightTopIconWidth=defSize;
    private float rightTopIconHeight=defSize;
    private float rightBottomIconWidth=defSize;
    private float rightBottomIconHeight=defSize;

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr){
        TypedArray typedArray=context.obtainStyledAttributes(attrs,R.styleable.lsVideoEditer,defStyleAttr,0);

        leftTopIconWidth=typedArray.getDimension(R.styleable.lsVideoEditer_leftTopIconWidth,defSize);
        leftTopIconHeight=typedArray.getDimension(R.styleable.lsVideoEditer_leftTopIconHeight,defSize);

        leftBottomIconWidth =typedArray.getDimension(R.styleable.lsVideoEditer_leftBottomIconWidth,defSize);
        leftBottomIconHeight =typedArray.getDimension(R.styleable.lsVideoEditer_leftBottomIconHeight,defSize);

        rightTopIconWidth =typedArray.getDimension(R.styleable.lsVideoEditer_rightTopIconWidth,defSize);
        rightTopIconHeight =typedArray.getDimension(R.styleable.lsVideoEditer_rightTopIconHeight,defSize);

        rightBottomIconWidth =typedArray.getDimension(R.styleable.lsVideoEditer_rightBottomIconWidth,defSize);
        rightBottomIconHeight =typedArray.getDimension(R.styleable.lsVideoEditer_rightBottomIconHeight,defSize);

        hotSize = typedArray.getDimension(R.styleable.lsVideoEditer_iconHotSize,50);

        clickGestureSize = typedArray.getDimension(R.styleable.lsVideoEditer_clickGestureSize,10);

        int leftTopIconIconId=typedArray.getResourceId(R.styleable.lsVideoEditer_leftTopIcon,0);
        int leftBottomIconId=typedArray.getResourceId(R.styleable.lsVideoEditer_leftBottomIcon,0);
        int rightTopIconId=typedArray.getResourceId(R.styleable.lsVideoEditer_rightTopIcon,0);
        int rightBottomIconId=typedArray.getResourceId(R.styleable.lsVideoEditer_rightBottomIcon,0);

        if(leftTopIconIconId!=0){
            leftTopIcon=getBitmap(context,leftTopIconIconId);
        }
        if(leftBottomIconId!=0){
            leftBottomIcon=getBitmap(context,leftBottomIconId);
        }
        if(rightTopIconId!=0){
            rightTopIcon=getBitmap(context,rightTopIconId);
        }
        if(rightBottomIconId!=0){
            rightBottomIcon=getBitmap(context,rightBottomIconId);
        }

        typedArray.recycle();
    }

    private static Bitmap getBitmap(Context context, int vectorDrawableId) {
        Bitmap bitmap ;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {//vector不能直接使用decodeResource
            Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        return bitmap;
    }



    //初始化
    private void init(){
//        resBitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.icon_test);

    }




    private Bitmap resBitmap;//可以拖动的bitmap

    private float hotSize=50;//热点区域的大小

    private float clickGestureSize=10;//touch判断是否是点击时候，down的x,y和up时xy的距离，如果在这个距离之下，则认为click，便会回调click事件

    private float touchX=0;//touch的x轴
    private float touchY=0;//touch的y轴的位置

    private float touchDownX=0;
    private float touchDownY=0;

    private Bitmap leftTopIcon;//左上角的图标
    private Bitmap rightTopIcon;//右上角的图标
    private Bitmap leftBottomIcon;//左下角的图标
    private Bitmap rightBottomIcon;//右下角的图标

    private int vWidth;
    private int vHeight;

    //存放bitmap的信息
    private List<Item> list=new LinkedList<>();


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if(vWidth==0) vWidth=getWidth();
        if(vHeight==0) vHeight=getHeight();

        Paint paint=new Paint();
        paint.setTextSize(50);


        if(resBitmap!=null){

            if(list.size()<=0){
                int centX=vWidth/2;
                int centY=vHeight/2;

                int resWidth=resBitmap.getWidth();
                int resHeight=resBitmap.getHeight();

                int iw=vWidth/2;
                int ih=iw*resHeight/resWidth;
                list.add(Item.createItem(centX-iw/2,centY-ih/2,centX+iw/2,centY+ih/2));
            }


            for(Item item:list){
                Matrix matrix = new Matrix();

                float tw=item.rectF.right-item.rectF.left;
                float th=item.rectF.bottom-item.rectF.top;

                float blX=tw/resBitmap.getWidth();
                float blY=th/resBitmap.getHeight();


                if(item.isFlip()){
                    matrix.postScale(-blX,blY,resBitmap.getWidth()/2,resBitmap.getHeight()/2);
                }else{
                    matrix.postScale(blX,blY,resBitmap.getWidth()/2,resBitmap.getHeight()/2);
                }

                float offsetLeft=(tw-resBitmap.getWidth())/2;//缩放导致的x轴的偏移量
                float offsetTop=(th-resBitmap.getHeight())/2;//缩放导致的y轴的偏移量

                matrix.postTranslate(item.rectF.left+offsetLeft,item.rectF.top+offsetTop);


                canvas.drawBitmap(resBitmap,matrix,paint);

                drawIcon(canvas,leftTopIcon,paint,item.rectF.left,item.rectF.top,leftTopIconWidth,leftTopIconHeight);
                drawIcon(canvas,leftBottomIcon,paint,item.rectF.left,item.rectF.bottom,leftBottomIconWidth,leftBottomIconHeight);
                drawIcon(canvas,rightBottomIcon,paint,item.rectF.right,item.rectF.bottom,rightBottomIconWidth,rightBottomIconHeight);
                drawIcon(canvas,rightTopIcon,paint,item.rectF.right,item.rectF.top,rightTopIconWidth,rightTopIconHeight);

            }
        }
    }


    private void drawIcon(Canvas canvas,Bitmap bitmap,Paint paint,float centX,float centY,float width,float height){
        if(bitmap==null){
            return;
        }

        Rect srcLeftTopIcon=new Rect(0,0,leftTopIcon.getWidth(),leftTopIcon.getHeight());

        int l= (int) (centX-width/2);
        int t= (int) (centY-height/2);
        int r= (int) (l+width);
        int b= (int) (t+height);

        Rect rectLeftTopIcon=new Rect(l,t,r,b);

        canvas.drawBitmap(bitmap,srcLeftTopIcon,rectLeftTopIcon,paint);
    }





    //手指选中的item
    private Item selectedItem;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        touchX=event.getX();
        touchY=event.getY();

        int action=event.getAction();

        if(action==MotionEvent.ACTION_DOWN){
            touchDownX=event.getX();
            touchDownY=event.getY();

            selectedItem=null;
            boolean isSelected=false;
            for(int i=list.size()-1;i>=0;i--){
                Item item=list.get(i);
                RectF rectF=item.getRectF();
                if(!isSelected&&touchX>rectF.left-hotSize&&touchX<rectF.right+hotSize&&touchY>rectF.top-hotSize&&touchY<rectF.bottom+hotSize){
                    //表明touch的点已经在该图片里面了，下面判断是否是在左下角和右下角，当在左下角和右下角时候，不能被拖动

                    if(rightBottomIcon!=null && touchX>(rectF.right-hotSize)&&touchY>(rectF.bottom-hotSize)){
                        item.setType(1);//缩放
                    }else if(leftBottomIcon!=null && touchX<(rectF.left+hotSize)&&touchY>(rectF.bottom-hotSize)){
                        item.setType(2);//翻转
                    }else if(rightTopIcon!=null && touchX>(rectF.right-hotSize)&&touchY<(rectF.top+hotSize)){
                        item.setType(3);//删除
                    }else if(touchX>rectF.left&&touchX<rectF.right&&touchY>rectF.top&&touchY<rectF.bottom){//如果移动的话还要不算热区
                        item.setType(0);//移动
                    }else{//表示按在热区外
                        continue;
                    }

                    item.setSelected(true);
                    isSelected=true;

                    //表示按住了右下角
                    item.setLeftDistance(touchX-rectF.left);
                    item.setTopDistance(touchY-rectF.top);

                    selectedItem=item;
                }else{
                    item.setSelected(false);
                }
            }
        }else if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL){//离手
            if(Math.abs(touchDownX-touchX)<clickGestureSize&&Math.abs(touchDownY-touchY)<clickGestureSize){//点击手势判断成功
                for(int i=list.size()-1;i>=0;i--){//循环便利，是否有选中的item
                    Item item=list.get(i);
                    if(item.isSelected()){//选中了
                        int type=item.getType();
                        if(type==2){//点击了翻转
                            onItemFlipClick(item);
                        }else if(type==3){//点击了删除
                            onItemDelteLick(item);
                        }
                    }

                }
            }

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


    //点击了右上角删除
    private void onItemDelteLick(Item item){
        Log.d("dddd","点击了删除");
        if(list.size()<=1){//最后一个不能删除
            Toast.makeText(getContext(),"最后一个啦~",Toast.LENGTH_SHORT).show();
        }else{
            list.remove(item);
            invalidate();
        }
    }
    ///点击了左下角翻转
    private void onItemFlipClick(Item item){
        Log.d("dddd","点击了翻转");
        item.setFlip(!item.isFlip());
        invalidate();
    }


    public void newItem(){
        if(resBitmap==null){
            return;
        }
        int nowCount=count();
        float itemWidth=getWidth()/3;
        float itemHeight=itemWidth*resBitmap.getHeight()/resBitmap.getWidth();
        float left=(nowCount-1)%3*itemWidth;
        float top=((nowCount-1)/3)*itemHeight;
        list.add(Item.createItem(left,top,left+itemWidth,top+itemHeight));
//        list.add(Item.createItem(0,0,itemWidth,itemHeight));

        invalidate();


    }
    public int count(){
        return list.size();
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
        chanageListItemsSize(resBitmap);

        invalidate();
    }

    ///切换图片后，需要批量修改item的属性
    private void chanageListItemsSize(Bitmap newBitmap){
        for(Item item:list){
            RectF rectf = item.getRectF();

            float scale=newBitmap.getHeight()/(float)newBitmap.getWidth();

            float nw=rectf.right-rectf.left;
            float nh=nw*scale;
            Log.d("dddd","======scale:"+scale+"   nw:"+nw+"    nh:"+nh+"     newBitmap.getHeight():"+newBitmap.getHeight()+"     newBitmap.getWidth():"+newBitmap.getWidth());
            RectF newRectf=new RectF(rectf.left,rectf.top,rectf.left+nw,rectf.top+nh);

            item.setRectF(newRectf);


        }
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
