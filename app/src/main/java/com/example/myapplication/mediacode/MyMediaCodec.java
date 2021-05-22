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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyMediaCodec implements Runnable{
    @Override
    public void run() {
        try {
            initExtractor();
            initDocoder();


            threadPool.execute(new InputThread());
            threadPool.execute(new OutThread());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public MyMediaCodec( String peopleFilePath,String bgFilePath){
        this.peopleFilePath=peopleFilePath;
        this.bgFilePath=bgFilePath;
    }


    private String peopleFilePath;
    private String bgFilePath;

    private ExecutorService threadPool= Executors.newFixedThreadPool(2);

    private MediaExtractor peopleExtractor;
    private MediaExtractor bgExtractor;


    private MediaCodec popleDecoder;
    private MediaCodec bgDecoder;

    private MediaCodec.BufferInfo popelBufferInfo=new MediaCodec.BufferInfo();
    private MediaCodec.BufferInfo bgBufferInfo=new MediaCodec.BufferInfo();

    private int peopleExtractorVideoTrackIndex=0;
    private int peopleExtractorAudioTrackIndex=0;

    private int bgExtractorVideoTrackIndex=0;
    private int bgExtractorAudioTrackIndex=0;

    private void initExtractor() throws IOException {
        peopleExtractor=new MediaExtractor();
        bgExtractor=new MediaExtractor();
        peopleExtractor.setDataSource(peopleFilePath);
        bgExtractor.setDataSource(bgFilePath);



        int count=peopleExtractor.getTrackCount();
        for(int i=0;i<count;i++){
            MediaFormat mediaFormat=peopleExtractor.getTrackFormat(i);
            String mime=mediaFormat.getString(MediaFormat.KEY_MIME);
            if(mime.startsWith("video")){//视频轨道
                peopleExtractorVideoTrackIndex=i;
            }else if(mime.startsWith("audio")){
                peopleExtractorAudioTrackIndex=i;
            }
        }
        count=bgExtractor.getTrackCount();
        for(int i=0;i<count;i++){
            MediaFormat mediaFormat=bgExtractor.getTrackFormat(i);
            String mime=mediaFormat.getString(MediaFormat.KEY_MIME);
            if(mime.startsWith("video")){//视频轨道
                bgExtractorVideoTrackIndex=i;
            }else if(mime.startsWith("audio")){
                bgExtractorAudioTrackIndex=i;
            }
        }
        peopleExtractor.selectTrack(peopleExtractorVideoTrackIndex);
        bgExtractor.selectTrack(bgExtractorVideoTrackIndex);
    }

    private void initDocoder() throws IOException {
        MediaFormat mediaFormat=peopleExtractor.getTrackFormat(peopleExtractorVideoTrackIndex);
        popleDecoder=MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
        popleDecoder.configure(mediaFormat,null,null,0);

        mediaFormat=bgExtractor.getTrackFormat(bgExtractorVideoTrackIndex);
        bgDecoder=MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
        bgDecoder.configure(mediaFormat,null,null,0);

        popleDecoder.start();
        bgDecoder.start();

    }

    private boolean isLiving=true;

    private final int TIMEOUT=20000000;

    private class InputThread implements Runnable{

        @Override
        public void run() {
            startInputData();
        }

        private void startInputData(){
            while (isLiving){
                int peopleInputBufferIndex=getMediacodecInputIndex(popleDecoder);
                int bgInputBufferIndex=getMediacodecInputIndex(bgDecoder);
                Log.d("dddd","向缓冲区输入数据peopleInputBufferIndex:"+peopleInputBufferIndex+"      bgInputBufferIndex:"+bgInputBufferIndex);

                ByteBuffer peopleInputBuffer=popleDecoder.getInputBuffer(peopleInputBufferIndex);
                ByteBuffer bgInputBuffer=bgDecoder.getInputBuffer(bgInputBufferIndex);

                //拿到两个缓冲区，然后提取数据解码
                int peopleSize=peopleExtractor.readSampleData(peopleInputBuffer,0);
                int bgSize=bgExtractor.readSampleData(bgInputBuffer,0);
                if(peopleSize<0){//无数据了
                    popleDecoder.queueInputBuffer(peopleInputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    bgDecoder.queueInputBuffer(bgInputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);

                    break;
                }
                if(bgSize<0){//背景没有数据了，那么从第一帧再重读
                    bgExtractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                    bgSize=bgExtractor.readSampleData(bgInputBuffer,0);
                }
                popleDecoder.queueInputBuffer(peopleInputBufferIndex, 0, peopleSize, peopleExtractor.getSampleTime(), 0);
                bgDecoder.queueInputBuffer(bgInputBufferIndex, 0, bgSize, bgExtractor.getSampleTime(), 0);

                //解码后会自动放入缓冲区，下面只需要去拿就好了

                peopleExtractor.advance();
                bgExtractor.advance();

            }
            isLiving=false;
        }
    }



    private class OutThread implements Runnable{
        @Override
        public void run() {
            out();
        }

        void out(){
            while(isLiving){
                int peopleIndex=getMediaCodecOutputIndex(popleDecoder,popelBufferInfo);
                int bgIndex=getMediaCodecOutputIndex(bgDecoder,bgBufferInfo);

                Image peopleImage=popleDecoder.getOutputImage(peopleIndex);
                Image bgImage=bgDecoder.getOutputImage(bgIndex);

                Bitmap peopleBitmap=imageToBitmap(peopleImage);
                Bitmap bgBitmap=imageToBitmap(bgImage);

                Canvas canvas=surfaceHolder.lockCanvas();
                drawBitmap(canvas,bgBitmap,800,800);
                drawBitmap(canvas,peopleBitmap,400,400);
                surfaceHolder.unlockCanvasAndPost(canvas);

                popleDecoder.releaseOutputBuffer(peopleIndex,false);
                bgDecoder.releaseOutputBuffer(bgIndex,false);
            }

            popleDecoder.stop();
            popleDecoder.release();

            bgDecoder.stop();
            popleDecoder.release();

            peopleExtractor.release();
            bgExtractor.release();

        }
    }

    private final Paint canvasPaint=new Paint();
    private void drawBitmap(Canvas canvas,Bitmap bitmap,int width,int height){
        Rect srcRect=new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        Rect dstRect=new Rect(0,0,width,height);

        canvas.drawBitmap(bitmap,srcRect,dstRect,canvasPaint);
    }

    private SurfaceHolder surfaceHolder;
    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
    }

    private int getMediaCodecOutputIndex(MediaCodec mediaCodec, MediaCodec.BufferInfo bufferInfo){
        int index=mediaCodec.dequeueOutputBuffer(bufferInfo,TIMEOUT);
        if(index>=0){
            return index;
        }
        return getMediaCodecOutputIndex(mediaCodec,bufferInfo);
    }

    private int getMediacodecInputIndex(MediaCodec mediaCodec){
        int index=mediaCodec.dequeueInputBuffer(TIMEOUT);
        if(index>=0){
            return index;
        }
        return getMediacodecInputIndex(mediaCodec);
    }



    private Bitmap imageToBitmap(Image image){
        YuvImage yuvImage = new YuvImage(YUV_420_888toNV21(image), ImageFormat.NV21, image.getWidth(),image.getHeight(), null);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0,  image.getWidth(),image.getHeight()), 80, stream);
        Bitmap bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());

        return bitmap;
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


}
