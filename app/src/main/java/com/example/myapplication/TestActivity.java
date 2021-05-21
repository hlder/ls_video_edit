package com.example.myapplication;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {
    ImageView imageView;


    int time=0;
    int index=0;
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        imageView=findViewById(R.id.imageView);



        String absolutePath=getExternalFilesDir("files").getAbsolutePath();

        String videoPath=absolutePath+"/a.mp4";
        Log.d("dddd","====================videoPath:"+videoPath);

        MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(videoPath);



        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View v) {

                Log.d("dddd","加载atTime:"+time+"    index:"+index);

//                Bitmap bitmap=mediaMetadataRetriever.getFrameAtIndex(index);
                Bitmap bitmap=mediaMetadataRetriever.getFrameAtTime(time);//time是微妙

                imageView.setImageBitmap(bitmap);

                index+=10;
                time+=500000;

            }
        });

    }


}
