package com.example.myapplication.mediacode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyThread2 implements Runnable{
    @Override
    public void run() {
        try {
            init();

            threadPool.execute(new InputThread());
            threadPool.execute(new OutThread());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public MyThread2(String videoFilePath){
        this.videoFilePath=videoFilePath;
    }

    private ExecutorService threadPool= Executors.newFixedThreadPool(2);

    private String videoFilePath;


    private boolean isLiving=true;

    private MediaExtractor mediaExtractor;
    private MediaCodec decodor;
    private MediaCodec.BufferInfo bufferInfo=new MediaCodec.BufferInfo();

    private void init() throws IOException {
        mediaExtractor=new MediaExtractor();
        mediaExtractor.setDataSource(videoFilePath);

        int count=mediaExtractor.getTrackCount();

        for(int i=0;i<count;i++){
            MediaFormat mediaFormat=mediaExtractor.getTrackFormat(i);
            String mime=mediaFormat.getString(MediaFormat.KEY_MIME);
            if(mime.startsWith("video")){//视频轨道

                mediaExtractor.selectTrack(i);
                decodor=MediaCodec.createDecoderByType(mime);
                decodor.configure(mediaFormat,null,null,0);
            }
        }

        decodor.start();
    }

    class InputThread implements Runnable{
        @Override
        public void run() {
            while(isLiving){
                int index=decodor.dequeueInputBuffer(1000000);
                Log.d("dddd","===============input线程："+index);
                if(index>=0){
                    ByteBuffer byteBuffer=decodor.getInputBuffer(index);
                    int sampleSize=mediaExtractor.readSampleData(byteBuffer,0);
                    if(sampleSize<0){///结束了
                        Log.d("dddd","===============2");
                        decodor.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        break;
                    }
                    decodor.queueInputBuffer(index, 0, sampleSize, mediaExtractor.getSampleTime(), 0);

                    mediaExtractor.advance();  //在MediaExtractor执行完一次readSampleData方法后，需要调用advance()去跳到下一个sample，然后再次读取数据
                }
            }

            Log.d("dddd","===============1");
            isLiving=false;
            decodor.stop();
            decodor.release();

            mediaExtractor.release();

        }
    }
    class OutThread implements Runnable{
        @Override
        public void run() {
            while (isLiving){
                try {
                    int index=decodor.dequeueOutputBuffer(bufferInfo,1000000);
                    if(index>=0){
                        Log.d("dddd","===============解码后：time:"+(bufferInfo.presentationTimeUs/1000/1000));

                        Image image=decodor.getOutputImage(index);

                        YuvImage yuvImage = new YuvImage(YUV_420_888toNV21(image), ImageFormat.NV21, image.getWidth(),image.getHeight(), null);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        yuvImage.compressToJpeg(new Rect(0, 0,  image.getWidth(),image.getHeight()), 80, stream);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());


                        Canvas canvas=surfaceHolder.lockCanvas();
                        canvas.drawBitmap(bitmap,0,0,new Paint());
                        surfaceHolder.unlockCanvasAndPost(canvas);



                        decodor.releaseOutputBuffer(index,false);
                    }

                }catch (IllegalStateException e){
                    e.printStackTrace();
                }


            }

        }
    }

    private static byte[] YUV_420_888toNV21(Image image) {
        byte[] nv21;
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();
        nv21 = new byte[ySize + uSize + vSize];
        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);
        return nv21;
    }




    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    public void setSurfaceView(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
        surfaceHolder=surfaceView.getHolder();
    }





}
