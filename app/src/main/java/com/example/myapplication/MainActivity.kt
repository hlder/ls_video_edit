package com.example.myapplication

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val mLsVideoEditer:LsVideoEditer = findViewById<LsVideoEditer>(R.id.mLsVideoEditer)
        mLsVideoEditer.setResBitmap(BitmapFactory.decodeResource(resources,R.mipmap.icon_test))


        findViewById<Button>(R.id.button1).setOnClickListener(View.OnClickListener {
            mLsVideoEditer.newItem()
        })
        findViewById<Button>(R.id.button2).setOnClickListener(View.OnClickListener {
            var index:Int=(((Math.random()*3)+1).toInt())
            if(index==2){
                mLsVideoEditer.setResBitmap(BitmapFactory.decodeResource(resources,R.mipmap.icon_test2))
            }else if(index==3){
                mLsVideoEditer.setResBitmap(BitmapFactory.decodeResource(resources,R.mipmap.icon_test3))
            }else{
                mLsVideoEditer.setResBitmap(BitmapFactory.decodeResource(resources,R.mipmap.icon_test))
            }
        })
    }
}