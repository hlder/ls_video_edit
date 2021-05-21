package com.example.myapplication.mediacode;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.SurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;

//https://blog.csdn.net/weixin_39175052/article/details/79367804?utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromMachineLearnPai2%7Edefault-2.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromMachineLearnPai2%7Edefault-2.control

public class MyThread implements Runnable{
    private final String peopleVideoFilePath  =       "/storage/emulated/0/Android/data/com.aliyun.ai.viapi/cache/out.mp4";
    private final String outVideoFilePath     =       "/storage/emulated/0/Android/data/com.aliyun.ai.viapi/files/test.mp4";

    private MediaCodec mediaCodec;

    private MediaExtractor mediaExtractor;
    private MediaMuxer mediaMuxer;

    private void init() throws IOException {
        mediaMuxer=new MediaMuxer(outVideoFilePath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);


        mediaExtractor=new MediaExtractor();
        mediaExtractor.setDataSource(peopleVideoFilePath);
        int extractorvideoTrackIndex = -1;
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) { //在videoExtractor的所以Track中遍历，找到视轨的id
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);//获得第id个Track对应的MediaForamt
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);//再获取该Track对应的KEY_MIME字段
            if (mime.startsWith("video/")) { //视轨的KEY_MIME是以"video/"开头的，音轨是"audio/"
                extractorvideoTrackIndex = i;
                break;
            }
        }
        MediaFormat mediaFormat=mediaExtractor.getTrackFormat(extractorvideoTrackIndex);



        mediaCodec=MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));

        //最后一个参数：1表示编码器，0解码器？
        mediaCodec.configure(mediaFormat,null,null,0);
        mediaCodec.start();

        int muxerTrachIndex = mediaMuxer.addTrack(mediaFormat);
        mediaMuxer.start();



        mediaExtractor.selectTrack(extractorvideoTrackIndex);

        int TIMEOUT_US=1000000;



        MediaCodec.BufferInfo bufferInfo=new MediaCodec.BufferInfo();
        while(true){
            int inputBufferIndex =mediaCodec.dequeueInputBuffer(TIMEOUT_US);
            if(inputBufferIndex>0){//获取到了缓冲区
                ByteBuffer inputBuffer=mediaCodec.getInputBuffer(inputBufferIndex);

                int sampleSize=mediaExtractor.readSampleData(inputBuffer,0);
                if(sampleSize<0){//数据全部读完了
//                isMediaEOS = true;
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    break;
                }else{
                    //将已写入数据的id为inputBufferIndex的ByteBuffer提交给MediaCodec进行解码
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, mediaExtractor.getSampleTime(), 0);

                    int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);  //获得已经成功解码的ByteBuffer的id
                    switch (outputBufferIndex) {
                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                            break;
                        default:


                            mediaMuxer.writeSampleData(muxerTrachIndex,inputBuffer,bufferInfo);

                            Log.d("dddd","===============================inputBuffer:"+inputBuffer+"     bufferInfo:"+bufferInfo);

                            mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                            break;
                    }

                    mediaExtractor.advance();  //在MediaExtractor执行完一次readSampleData方法后，需要调用advance()去跳到下一个sample，然后再次读取数据
                }
            }
        }





        mediaMuxer.stop();
        mediaMuxer.release();


        mediaCodec.stop();
        mediaCodec.release();


        mediaExtractor.release();


    }



    @Override
    public void run() {
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}
