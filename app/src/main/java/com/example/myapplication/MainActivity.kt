package com.example.myapplication

import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<LsVideoEditer>(R.id.mTestView).setResBitmap(BitmapFactory.decodeResource(resources,R.mipmap.icon_test))
    }
}