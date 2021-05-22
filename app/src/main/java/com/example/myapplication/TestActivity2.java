package com.example.myapplication;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.mediacode.MyMediaCodec;
import com.example.myapplication.mediacode.MyThread2;

public class TestActivity2 extends AppCompatActivity {
    SurfaceView surfaceView;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);

        surfaceView=findViewById(R.id.surfaceView);


        Button button=findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new Thread(new MyThread()).start();


                String filePath=getExternalFilesDir(null).getPath()+"/a.mp4";
                String bgFilePath=getExternalFilesDir(null).getPath()+"/b.mp4";
//                String filePath="/sdcard/Android/data/com.example.myapplication/files/a.mp4";
                Log.d("dddd","=====filePath:"+filePath);

                MyMediaCodec myMediaCodec=new MyMediaCodec(filePath,bgFilePath);
                myMediaCodec.setSurfaceHolder(surfaceView.getHolder());
                new Thread(myMediaCodec).start();

//                MyThread2 myThread2=new MyThread2( filePath);
//                myThread2.setSurfaceView(surfaceView);
//                new Thread(myThread2).start();

            }
        });

    }




}
