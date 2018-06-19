package com.max_plus.homedooropenplate.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.max_plus.homedooropenplate.R;

public class SettingActivity extends Activity implements View.OnClickListener {
    private ImageView iv_setback;
    private TextView tv_set_time, tv_setloginpsd, tv_setopenpsd, tv_setopenchoose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        iv_setback = findViewById(R.id.iv_setback);
        iv_setback.setOnClickListener(this);

//        tv_set_time = findViewById(R.id.tv_set_time);
//        tv_set_time.setOnClickListener(this);

        tv_setloginpsd = findViewById(R.id.tv_setloginpsd);
        tv_setloginpsd.setOnClickListener(this);

        tv_setopenpsd = findViewById(R.id.tv_setopenpsd);
        tv_setopenpsd.setOnClickListener(this);

        tv_setopenchoose = findViewById(R.id.tv_setopenchoose);
        tv_setopenchoose.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_setback:
                finish();
                break;
//            case R.id.tv_set_time:
//                Intent intent = new Intent(this, SetTimeActivity.class);
//                startActivity(intent);
//                break;
            case R.id.tv_setloginpsd:
                Intent intent = new Intent(this, SetLoginPsdActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_setopenpsd:
                Intent intent1 = new Intent(this, SetOpenPsdActivity.class);
                startActivity(intent1);
                break;
            case R.id.tv_setopenchoose:
                Intent intent2 = new Intent(this, SetChooseOpenActivity.class);
                startActivity(intent2);
                break;
        }
    }
}
