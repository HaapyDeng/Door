package com.max_plus.homedooropenplate.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.max_plus.homedooropenplate.R;

public class SetOpenPsdActivity extends Activity implements View.OnClickListener {
    private ImageView iv_setback;
    private EditText et_setoldpsd, et_setnewpsd, et_setnewpsdagain;
    private TextView tv_save;
    private String oldPsd, newPsd, newNewPsd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_open_psd);
        iv_setback = findViewById(R.id.iv_setback);
        iv_setback.setOnClickListener(this);

        tv_save = findViewById(R.id.tv_save);
        tv_save.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_setback:
                finish();
                break;
            case R.id.tv_save:
                et_setoldpsd = findViewById(R.id.et_setoldpsd);
                oldPsd = et_setoldpsd.getText().toString().trim();
                et_setnewpsd = findViewById(R.id.et_setnewpsd);
                newPsd = et_setnewpsd.getText().toString().trim();
                et_setnewpsdagain = findViewById(R.id.et_setnewpsdagain);
                newNewPsd = et_setnewpsdagain.getText().toString().trim();
                if (oldPsd.length() == 0 || newPsd.length() == 0 || newNewPsd.length() == 0) {
                    Toast.makeText(this, "密码不能为空，请输入密码", Toast.LENGTH_LONG).show();
                    break;
                }
                if (!newPsd.equals(newNewPsd)) {
                    Toast.makeText(this, "新密码和再次输入的新密码不匹配，请重新输入", Toast.LENGTH_LONG).show();
                    break;
                }
                SharedPreferences sp = getSharedPreferences("openpassword", Context.MODE_PRIVATE);
                String psd = sp.getString("openpsd", null);
                if (psd == null) {
                    SharedPreferences sp3 = getSharedPreferences("openpassword", Context.MODE_PRIVATE);
                    sp3.edit().putString("openpsd", "888888").commit();
                    psd ="888888";
                }
                if (!psd.equals(oldPsd)) {
                    Toast.makeText(this, "旧密码输入错误，请重新输入", Toast.LENGTH_LONG).show();
                    break;
                }
                SharedPreferences sp2 = getSharedPreferences("openpassword", Context.MODE_PRIVATE);
                sp2.edit().putString("openpsd", newPsd).commit();
                Toast.makeText(this, "修改密码成功", Toast.LENGTH_LONG).show();
                finish();
                break;

        }
    }
}
