package com.max_plus.homedooropenplate.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.max_plus.homedooropenplate.R;

import java.util.List;

import static com.max_plus.homedooropenplate.GetPicturesPath.getImagePathFromSD;


public class StartActivity extends AppCompatActivity {
    private List<String> list;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        //获取sd卡相册所有图片路径
//        list.addAll(getImagePathFromSD());
//        for (String imagePath : list) {
//            System.out.println(imagePath);
//        }
        new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }.sendEmptyMessageDelayed(0, 3000);
    }
}
