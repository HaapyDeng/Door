package com.max_plus.homedooropenplate.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.max_plus.homedooropenplate.R;

public class SetChooseOpenActivity extends Activity implements View.OnClickListener {
    private ImageView iv_setback;
    private TextView tv_save;
    private Switch switch_chipopen, switch_tsdpopen, switch_psd_code_open, switch_psdpopen;
    private int flag_switch_chipopen = 1, flag_switch_tsdpopen = 1, flag_switch_psd_code_open = 1, flag_switch_psdpopen = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_choose_open);
        iv_setback = findViewById(R.id.iv_setback);
        iv_setback.setOnClickListener(this);

        tv_save = findViewById(R.id.tv_save);
        tv_save.setOnClickListener(this);

        switch_chipopen = findViewById(R.id.switch_chipopen);
        switch_chipopen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    flag_switch_chipopen = 1;
                } else {
                    flag_switch_chipopen = 0;
                }
            }
        });

        switch_tsdpopen = findViewById(R.id.switch_tsdpopen);
        switch_tsdpopen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    flag_switch_tsdpopen = 1;
                } else {
                    flag_switch_tsdpopen = 0;
                }
            }
        });

        switch_psd_code_open = findViewById(R.id.switch_psd_code_open);
        switch_psd_code_open.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    flag_switch_psd_code_open = 1;
                } else {
                    flag_switch_psd_code_open = 0;
                }
            }
        });

        switch_psdpopen = findViewById(R.id.switch_psdpopen);
        switch_psdpopen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    flag_switch_psdpopen = 1;
                } else {
                    flag_switch_psdpopen = 0;
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_setback:
                finish();
                break;
            case R.id.tv_save:
                SharedPreferences sp = getSharedPreferences("flag", Context.MODE_PRIVATE);
                sp.edit().putInt("switch_chipopen", flag_switch_chipopen).commit();
                sp.edit().putInt("switch_tsdpopen", flag_switch_tsdpopen).commit();
                sp.edit().putInt("switch_psd_code_open", flag_switch_psd_code_open).commit();
                sp.edit().putInt("switch_psdpopen", flag_switch_psdpopen).commit();

                Log.d("sp1==>>", "" + sp.getInt("switch_chipopen", 0));
                Log.d("sp2==>>", "" + sp.getInt("switch_tsdpopen", 0));
                Log.d("sp3==>>", "" + sp.getInt("switch_psd_code_open", 0));
                Log.d("sp4==>>", "" + sp.getInt("switch_psdpopen", 0));
                Toast.makeText(this, "保存成功", Toast.LENGTH_LONG).show();
                finish();
                break;
        }
    }

}
