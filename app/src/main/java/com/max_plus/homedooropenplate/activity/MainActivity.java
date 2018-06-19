package com.max_plus.homedooropenplate.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.icu.text.LocaleDisplayNames;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.max_plus.homedooropenplate.DetecterActivity;
import com.max_plus.homedooropenplate.R;
import com.max_plus.homedooropenplate.RegisterActivity;
import com.max_plus.homedooropenplate.zxing.activity.CaptureActivity;
//import com.yzq.zxinglibrary.android.CaptureActivity;
//import com.yzq.zxinglibrary.bean.ZxingConfig;
//import com.yzq.zxinglibrary.common.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;

import cn.jpush.android.api.JPushInterface;
import cz.msebera.android.httpclient.Header;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

import static com.max_plus.homedooropenplate.getUrlCode.URLRequest;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int msgTime = 1;
    private TextView tv_time_year, tv_time_hour;
    private Button bt_callOpen, bt_autoOpen, bt_ok, bt_nameok, bt_psdopen, bt_opencodeopen, bt_face_open, bt_regist;
    private FrameLayout frameLayout;
    private Dialog mCameraDialog2;
    private Dialog mCameraDialog;
    private RelativeLayout rl_main;
    private ImageView iv_back, back_name, back_psdopen, tv_setting;
    private View call_userFragment, mainFragment, videoView, verfiy_name, passwodrOpen, openSuccess, openCodeOpen;
    private LinearLayout fl_replace;
    private EditText et_doorNum, et_userNum, et_psdopen, et_opencodeopen;
    private String doorNum;
    private String mac, userName;
    private Long currentTime;
    //    private static final String ip = "http://192.168.1.119:100";//线下测试地址
    private static final String ip = "http://101.201.28.83:100";//线上地址
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    private RtcEngine mRtcEngine;// Tutorial Step 1
    private ImageView tv_close_video, back_open, back_opencodeopen;
    private int channel;//创建的房间号
    private int userId = 100;
    private String allName;
    private int typeFlag = 0;

    //视频通话hander
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { // Tutorial Step 5
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) { // Tutorial Step 7
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft();
                }
            });
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) { // Tutorial Step 10
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserVideoMuted(uid, muted);
                }
            });
        }
    };

    // Tutorial Step 1：初始化视频通话引擎
    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e("exception>>", Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    // Tutorial Step 2：设置视频属性
    private void setupVideoProfile() {
        mRtcEngine.enableVideo();
        mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, true);
    }

    // Tutorial Step 3：设置本地显示视频
    private void setupLocalVideo() {
        FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        surfaceView.setZOrderMediaOverlay(true);
        container.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, 0));
    }

    // Tutorial Step 4：加入频道
    private void joinChannel() {
        mRtcEngine.joinChannel(null, "" + channel, "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
    }

    // Tutorial Step 5：连接视频
    private void setupRemoteVideo(int uid) {
        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);

        if (container.getChildCount() >= 1) {
            return;
        }

        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        container.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));

        surfaceView.setTag(uid); // for mark purpose
//        View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
//        tipMsg.setVisibility(View.GONE);
    }

    // Tutorial Step 6：离开频道
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    // Tutorial Step 7
    private void onRemoteUserLeft() {
        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
        container.removeAllViews();
        leaveChannel();
        fl_replace.removeView(videoView);
        mainFragment.setVisibility(View.VISIBLE);

//        View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
//        tipMsg.setVisibility(View.VISIBLE);
    }

    // Tutorial Step 10
    private void onRemoteUserVideoMuted(int uid, boolean muted) {
        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);

        SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);

        Object tag = surfaceView.getTag();
        if (tag != null && (Integer) tag == uid) {
            surfaceView.setVisibility(muted ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //用极光推送设置别名
        mac = getLocalMac(this);
        JPushInterface.setAlias(this, 1, mac.replace(":", ""));
        Log.d("Alias==>>>", mac.replace(":", ""));
        //判断是否是极光推送消息来打开主界面
        Intent intent = getIntent();
        String a = intent.getStringExtra("open");
        userId = intent.getIntExtra("uid", 0);
        Log.d("a==>>", "111" + a + userId);
        fl_replace = findViewById(R.id.fl_replace);
        if (a != null) {
            doSendLog(userId);
            userId = 100;
            openSuccess = LayoutInflater.from(MainActivity.this).inflate(R.layout.open_door_success, null);
            fl_replace.addView(openSuccess);
            back_open = openSuccess.findViewById(R.id.back_open);
            back_open.setOnClickListener(this);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(3000);
//                        openSuccess.setVisibility(View.GONE);
//                        mainFragment.setVisibility(View.VISIBLE);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
        }
        //显示时间
        tv_time_year = findViewById(R.id.tv_time_set);
        tv_time_hour = findViewById(R.id.tv_time);
        new TimeThread().start();
        //设置按钮
        tv_setting = findViewById(R.id.tv_setting);
        tv_setting.setOnClickListener(this);
        //填充主view
        mainFragment = LayoutInflater.from(MainActivity.this).inflate(R.layout.main_u_fragment, null);
        fl_replace.addView(mainFragment);
        bt_autoOpen = mainFragment.findViewById(R.id.bt_auto_open);
        bt_autoOpen.setOnClickListener(this);
        bt_callOpen = mainFragment.findViewById(R.id.bt_call_open);
        bt_callOpen.setOnClickListener(this);
        bt_face_open = mainFragment.findViewById(R.id.bt_face_open);
        bt_face_open.setOnClickListener(this);

//        bt_regist = mainFragment.findViewById(R.id.bt_regist);
//        bt_regist.setOnClickListener(this);

    }

    private void startDetector(int camera) {
        Log.d("camera==>>>", "" + camera);
        Intent it = new Intent(MainActivity.this, DetecterActivity.class);
        it.putExtra("Camera", camera);
        startActivityForResult(it, 3);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.bt_regist://注册人脸
//                Intent fintent = new Intent("android.media.action.IMAGE_CAPTURE");
//                ContentValues values = new ContentValues(1);
//                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//                ((com.max_plus.homedooropenplate.Application) (MainActivity.this.getApplicationContext())).setCaptureImage(uri);
//                fintent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//                startActivityForResult(fintent, 1);
//                break;
            case R.id.bt_face_open://人脸识别开门
//                if (((Application)getApplicationContext()))
//                if (((com.max_plus.homedooropenplate.Application)MainActivity.this.getApplicationContext())) {
//                    Toast.makeText(this, "没有注册人脸，请先注册！", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(this, "有注册人脸！", Toast.LENGTH_SHORT).show();
//                }
                startDetector(0);//1代表开启前置摄像头 0代表开启后置摄像头
                break;
            case R.id.tv_setting:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.back_open://开门成功返回按钮

                openSuccess.setVisibility(View.GONE);
                mainFragment.setVisibility(View.VISIBLE);
//                mainFragment = LayoutInflater.from(MainActivity.this).inflate(R.layout.main_u_fragment, null);
//                fl_replace.addView(mainFragment);
                break;
            case R.id.psd_open: //密码开门
                mCameraDialog.dismiss();
                mainFragment.setVisibility(View.GONE);
                passwodrOpen = LayoutInflater.from(MainActivity.this).inflate(R.layout.password_open_fragment, null);
                fl_replace.addView(passwodrOpen);
                et_psdopen = passwodrOpen.findViewById(R.id.et_psdopen);
                bt_psdopen = passwodrOpen.findViewById(R.id.bt_psdopen);
                bt_psdopen.setOnClickListener(this);
                back_psdopen = passwodrOpen.findViewById(R.id.back_psdopen);
                back_psdopen.setOnClickListener(this);
                break;
            case R.id.bt_psdopen://密码开门确认按钮
                SharedPreferences sp = getSharedPreferences("openpassword", Context.MODE_PRIVATE);
                String psd = sp.getString("openpsd", null);
                if (psd == null) {
                    SharedPreferences sp3 = getSharedPreferences("openpassword", Context.MODE_PRIVATE);
                    sp3.edit().putString("openpsd", "888888").commit();
                    psd = "888888";
                }
                et_psdopen = passwodrOpen.findViewById(R.id.et_psdopen);
                String password = et_psdopen.getText().toString().trim();
                if (password.equals("")) {
                    Toast.makeText(this, "密码不能为空，请输入开门密码", Toast.LENGTH_LONG).show();
                    break;
                }
                if (psd.equals(password)) {
                    //开门成功
                    passwodrOpen.setVisibility(View.GONE);
                    openSuccess = LayoutInflater.from(MainActivity.this).inflate(R.layout.open_door_success, null);
                    fl_replace.addView(openSuccess);
                    back_open = openSuccess.findViewById(R.id.back_open);
                    back_open.setOnClickListener(MainActivity.this);
                } else {
                    Toast.makeText(this, "输入的开门密码错误，请重新输入", Toast.LENGTH_LONG).show();
                    break;
                }
                break;
            case R.id.back_psdopen://密码开门返回按钮
                passwodrOpen.setVisibility(View.GONE);
                mainFragment.setVisibility(View.VISIBLE);
                break;
            case R.id.tdc_open://二维码开门
                mCameraDialog.dismiss();
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
                    Intent intentt = new Intent(MainActivity.this,
                            Capture2Activity.class);
                    startActivityForResult(intentt, 0);
                }

//                Intent tdsIntent = new Intent(MainActivity.this,
//                        CaptureActivity.class);
//                ZxingConfig config = new ZxingConfig();
//                config.setShowbottomLayout(false);//底部布局（包括闪光灯和相册）
//                config.setPlayBeep(true);//是否播放提示音
//                config.setShake(true);//是否震动
//                config.setShowAlbum(false);//是否显示相册
//                config.setShowFlashLight(false);//是否显示闪光灯
//                tdsIntent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
//                startActivityForResult(tdsIntent, 0);
                break;
            case R.id.code_open://开门码开门
                mCameraDialog.dismiss();
                mainFragment.setVisibility(View.GONE);
                openCodeOpen = LayoutInflater.from(MainActivity.this).inflate(R.layout.open_code_open_fragment, null);
                fl_replace.addView(openCodeOpen);
                back_opencodeopen = openCodeOpen.findViewById(R.id.back_opencodeopen);
                back_opencodeopen.setOnClickListener(this);

                bt_opencodeopen = openCodeOpen.findViewById(R.id.bt_opencodeopen);
                bt_opencodeopen.setOnClickListener(this);
                break;
            case R.id.back_opencodeopen://开门码开门返回按钮
                openCodeOpen.setVisibility(View.GONE);
                mainFragment.setVisibility(View.VISIBLE);
                break;
            case R.id.bt_opencodeopen://开门码开门确认按钮
                et_opencodeopen = openCodeOpen.findViewById(R.id.et_opencodeopen);
                String openCode = et_opencodeopen.getText().toString().trim();
                if (openCode.equals("")) {
                    Toast.makeText(this, "开门码不能为空", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    doOpenCodeOpenDoor(Integer.parseInt(openCode));
                }
                break;
            case R.id.cancel://取消
                mCameraDialog.dismiss();
                break;
            case R.id.bt_auto_open://弹出自助开门dialog
                setAutoDialog();
                break;
            case R.id.bt_call_open://弹出呼叫开门dialog
                setCallDialog();
                break;
            case R.id.call_user://呼叫户主页面
                // fl_replace.removeView(mainFragment);
                mainFragment.setVisibility(View.GONE);
                call_userFragment = LayoutInflater.from(MainActivity.this).inflate(R.layout.call_user_fragment, null);
                fl_replace.addView(call_userFragment);
                mCameraDialog2.dismiss();

                bt_ok = call_userFragment.findViewById(R.id.bt_ok);
                bt_ok.setOnClickListener(this);
                iv_back = call_userFragment.findViewById(R.id.back);
                iv_back.setOnClickListener(this);
                et_doorNum = call_userFragment.findViewById(R.id.et_doorNum);
                break;
            case R.id.call_Property://呼叫物管页面
                mCameraDialog2.dismiss();
                doCallPropertyManagement();
                break;
            case R.id.cancel2:
                mCameraDialog2.dismiss();
                break;
            case R.id.bt_ok://呼叫户主开启视频
                doorNum = et_doorNum.getText().toString().trim();
                if (doorNum.equals("")) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.pl_put_in_door_no), Toast.LENGTH_LONG).show();
                    return;
                }
                checkDoorNum(doorNum);
                break;
            case R.id.bt_nameok://检测姓氏
                userName = et_userNum.getText().toString().trim();
                if (userName.length() == 0) {
                    Toast.makeText(MainActivity.this, "用户姓氏不能为空", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    doVerifyName(mac, doorNum, userName);
                }

                break;
            case R.id.back:
                call_userFragment.setVisibility(View.GONE);
                mainFragment.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_close_video://关闭视频通话
                Log.d("leaveChannel>>>", "leaveChannel");
                leaveChannel();
                fl_replace.removeView(videoView);
                mainFragment.setVisibility(View.VISIBLE);
                break;
            case R.id.back_name:
                verfiy_name.setVisibility(View.GONE);
                call_userFragment.setVisibility(View.VISIBLE);
                break;
        }
    }

    //开门成功返回服务器log
    private void doSendLog(int Id) {
        mac = getLocalMac(this);
        currentTime = getCurrentTime();
        Log.d("currentTime", "" + currentTime);
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("M-Sign", "1");
        client.addHeader("M-Timestamp", "" + currentTime);
        RequestParams params = new RequestParams();
        params.put("type", 1);
        params.put("id", Id);
        Log.d("url==>>>", ip + "/log/" + mac);
        client.post(ip + "/log/" + mac, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("rep", response.toString());
                try {
                    int code = response.getInt("code");
                    if (code == 0) {
                        Log.d("message:", "sendLog success");
                    } else {
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(MainActivity.this, responseString, Toast.LENGTH_LONG).show();
                return;
            }
        });
    }

    //开门码开门
    private void doOpenCodeOpenDoor(int openCode) {
        mac = getLocalMac(this);
        currentTime = getCurrentTime();
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("M-Sign", "1");
        client.addHeader("M-Timestamp", "" + currentTime);
        Log.d("url==>>>", ip + "/validate/code/" + mac + "/" + openCode);
        client.get(ip + "/validate/code/" + mac + "/" + openCode, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("rep", response.toString());
                try {
                    int code = response.getInt("code");
                    if (code == 0) {
                        JSONObject data = response.getJSONObject("data");
                        userId = data.getInt("id");
                        allName = data.getString("name");
                        Log.d("uID", "" + userId);
                        doSendLog(userId);
                        openCodeOpen.setVisibility(View.GONE);
                        openSuccess = LayoutInflater.from(MainActivity.this).inflate(R.layout.open_door_success, null);
                        fl_replace.addView(openSuccess);
                        back_open = openSuccess.findViewById(R.id.back_open);
                        back_open.setOnClickListener(MainActivity.this);
                    } else {
                        Toast.makeText(MainActivity.this, response.getString("message"), Toast.LENGTH_LONG).show();
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(MainActivity.this, responseString, Toast.LENGTH_LONG).show();
                return;
            }
        });
    }

    //呼叫物管开启视频
    private void doCallPropertyManagement() {
        mac = getLocalMac(this);
        currentTime = getCurrentTime();
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("M-Sign", "1");
        client.addHeader("M-Timestamp", "" + currentTime);
        Log.d("url==>>>", ip + "/call/property/" + mac);
        client.get(ip + "/call/property/" + mac, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("rep", response.toString());
                try {
                    int code = response.getInt("code");
                    if (code == 0) {
                        //加入聊天视图
                        channel = response.getInt("data");
                        mainFragment.setVisibility(View.GONE);
                        videoView = LayoutInflater.from(MainActivity.this).inflate(R.layout.video_chat_view, null);
                        fl_replace.addView(videoView);
                        tv_close_video = videoView.findViewById(R.id.tv_close_video);
                        tv_close_video.setOnClickListener(MainActivity.this);
                        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
                            initAgoraEngineAndJoinChannel();//初始化视频通话及加入视频聊天
                        }
                        return;
                    } else {
                        Toast.makeText(MainActivity.this, response.getString("message"), Toast.LENGTH_LONG).show();
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_LONG).show();
                return;
            }
        });
    }

    //检查房号是否正确
    private void checkDoorNum(final String doorNum) {
        mac = getLocalMac(this);
        currentTime = getCurrentTime();
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("M-Sign", "1");
        client.addHeader("M-Timestamp", "" + currentTime);
//        RequestParams requestParams = new RequestParams();
//        requestParams.put("mac", mac);
//        requestParams.put("name", doorNum);
        Log.d("url==>>>", ip + "/validate/house/" + mac + "/" + doorNum);
        client.get(ip + "/validate/house/" + mac + "/" + doorNum, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("log>>>", response.toString());
                try {
                    int code = response.getInt("code");
                    if (code == 0) {
                        JSONObject typeObject = response.getJSONObject("data");
                        int type = typeObject.getInt("type");
                        Log.d("type", "" + type);
                        typeFlag = type;
                        if (type == 3) {
                            call_userFragment.setVisibility(View.GONE);
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            // 获取软键盘的显示状态
                            boolean isOpen = imm.isActive();
                            // 如果软键盘已经显示，则隐藏，反之则显示
                            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                            //开启呼叫用户视频通话
                            doCallOwner(mac, doorNum);
                            return;
                        } else {
                            //验证用户姓氏
                            call_userFragment.setVisibility(View.GONE);
                            verfiy_name = LayoutInflater.from(MainActivity.this).inflate(R.layout.verfiy_name_fragment, null);
                            fl_replace.addView(verfiy_name);
                            et_userNum = verfiy_name.findViewById(R.id.et_userNum);

                            bt_nameok = verfiy_name.findViewById(R.id.bt_nameok);
                            bt_nameok.setOnClickListener(MainActivity.this);

                            back_name = verfiy_name.findViewById(R.id.back_name);
                            back_name.setOnClickListener(MainActivity.this);
                            return;

                        }


                    } else {
                        Toast.makeText(MainActivity.this, response.getString("message"), Toast.LENGTH_LONG).show();
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_LONG).show();
                return;
            }
        });

    }

    //验证用户姓氏
    private void doVerifyName(final String mac, final String doorNum, String userName) {
        currentTime = getCurrentTime();
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("M-Sign", "1");
        client.addHeader("M-Timestamp", "" + currentTime);
        Log.d("url", ip + "/validate/username/" + mac + "/" + doorNum + "/" + userName);
        client.get(ip + "/validate/username/" + mac + "/" + doorNum + "/" + userName, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("rep", response.toString());
                try {
                    int code = response.getInt("code");
                    if (code == 0) {

                        //开启呼叫用户视频通话
                        doCallOwner(mac, doorNum);
                    } else {
                        Log.d("logaa", "dd");
                        Toast.makeText(MainActivity.this, response.getString("message"), Toast.LENGTH_LONG).show();
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_LONG).show();
                return;
            }
        });
    }

    //呼叫用户
    private void doCallOwner(String mac, String doorNum) {
        currentTime = getCurrentTime();
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("M-Sign", "1");
        client.addHeader("M-Timestamp", "" + currentTime);
        Log.d("url==>>>", ip + "/call/owner/" + mac + "/" + doorNum);
        client.get(ip + "/call/owner/" + mac + "/" + doorNum, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("rep", response.toString());
                try {
                    int code = response.getInt("code");
                    if (code == 0) {
                        Log.d("typeFlag", "" + typeFlag);
                        if (typeFlag == 3) {
                            call_userFragment.setVisibility(View.GONE);
                        } else {
                            verfiy_name.setVisibility(View.GONE);
                        }
                        JSONObject data = response.getJSONObject("data");
                        userId = data.getInt("id");
                        Log.d("uID", "" + userId);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        // 获取软键盘的显示状态
                        boolean isOpen = imm.isActive();
                        // 如果软键盘已经显示，则隐藏，反之则显示
                        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                        //加入聊天视图
                        channel = data.getInt("number");
                        videoView = LayoutInflater.from(MainActivity.this).inflate(R.layout.video_chat_view, null);
                        fl_replace.addView(videoView);
                        tv_close_video = videoView.findViewById(R.id.tv_close_video);
                        tv_close_video.setOnClickListener(MainActivity.this);
                        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
                            initAgoraEngineAndJoinChannel();//初始化视频通话及加入视频聊天
                        }

                        return;
                    } else {
                        Log.d("adsd", "sdsd");
                        Toast.makeText(MainActivity.this, response.getString("message"), Toast.LENGTH_LONG).show();
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(MainActivity.this, responseString, Toast.LENGTH_LONG).show();
                return;
            }
        });
    }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();     // Tutorial Step 1
        setupVideoProfile();         // Tutorial Step 2
        setupLocalVideo();           // Tutorial Step 3
        joinChannel();               // Tutorial Step 4
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //显示扫描到的内容
        if (requestCode == 0) {
            //显示扫描到的内容
//            Bundle bundled = data.getExtras();
            //显示扫描到的内容
            if (data == null) {
                Toast.makeText(MainActivity.this, "未能识别二维码，请重试", Toast.LENGTH_LONG).show();
                return;
            }
            if (data.getStringExtra("result").equals("0")) {
                Toast.makeText(MainActivity.this, "未能识别二维码，请重试", Toast.LENGTH_LONG).show();
                return;
            }
            String data2 = data.getStringExtra("result");
            Log.d("data2===>", data2);
            //解析出url中的code
            String code2 = URLRequest(data2).get("code");
            if (code2 == null) {
                Toast.makeText(this, "二维码图片错误", Toast.LENGTH_LONG).show();
                return;
            } else {
                Log.d("code===>", code2);
                int codee = Integer.parseInt(code2);
                Log.d("codee===>", "" + codee);
                doTdcOpen(codee);
            }

        } else if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri mPath = ((com.max_plus.homedooropenplate.Application) (MainActivity.this.getApplicationContext())).getCaptureImage();
            String file = getPath(mPath);
            Bitmap bmp = com.max_plus.homedooropenplate.Application.decodeImage(file);
            startRegister(bmp, file);
        } else if (requestCode == 3) {
            int open = data.getIntExtra("open", 2);
            Log.d("open==>>>", "" + open);
            if (open == 1) {
                //识别成功
                mainFragment.setVisibility(View.GONE);
                openSuccess = LayoutInflater.from(MainActivity.this).inflate(R.layout.open_door_success, null);
                fl_replace.addView(openSuccess);
                back_open = openSuccess.findViewById(R.id.back_open);
                back_open.setOnClickListener(this);
            } else {
                Toast.makeText(MainActivity.this, "未识别，请重新识别或选择其他开门方式", Toast.LENGTH_LONG).show();
                return;
            }
        }

    }

    /**
     * @param mBitmap
     */
    private void startRegister(Bitmap mBitmap, String file) {
        Intent it = new Intent(MainActivity.this, RegisterActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("imagePath", file);
        it.putExtras(bundle);
        startActivityForResult(it, 4);
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     * @return
     */
    private String getPath(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(this, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(this, contentUri, null, null);
                } else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    return getDataColumn(this, contentUri, selection, selectionArgs);
                }
            }
        }
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor actualimagecursor = this.getContentResolver().query(uri, proj, null, null, null);
        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        actualimagecursor.moveToFirst();
        String img_path = actualimagecursor.getString(actual_image_column_index);
        String end = img_path.substring(img_path.length() - 4);
        if (0 != end.compareToIgnoreCase(".jpg") && 0 != end.compareToIgnoreCase(".png")) {
            return null;
        }
        return img_path;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    //上传识别的二维码内容开门
    private void doTdcOpen(int codee) {
        currentTime = getCurrentTime();
        mac = getLocalMac(this);
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("M-Sign", "1");
        client.addHeader("M-Timestamp", "" + currentTime);
        client.get(ip + "/validate/qr-code/" + mac + "/" + codee, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("rsp==.>", response.toString());
                try {
                    int code = response.getInt("code");
                    if (code == 0) {
                        JSONObject data = response.getJSONObject("data");
                        userId = data.getInt("id");
                        Log.d("uID", "" + userId);
                        doSendLog(userId);
                        openSuccess = LayoutInflater.from(MainActivity.this).inflate(R.layout.open_door_success, null);
                        fl_replace.addView(openSuccess);
                        back_open = openSuccess.findViewById(R.id.back_open);
                        back_open.setOnClickListener(MainActivity.this);
                    } else {
                        Toast.makeText(MainActivity.this, response.getString("message"), Toast.LENGTH_LONG).show();
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(MainActivity.this, responseString, Toast.LENGTH_LONG).show();
                return;
            }


        });
    }

    //获取实时时间线程
    public class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = msgTime;
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msgTime://时间戳
                    tv_time_year.setText(getTimeYear());
                    tv_time_hour.setText(getTimeHour());
                    break;
                default:
                    break;
            }
        }
    };

    //弹出底部菜单
    private void setAutoDialog() {
        mCameraDialog = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.auto_bottom_dialog, null);
        //判断根据设置开门选项显示界面
        SharedPreferences sp = getSharedPreferences("flag", Context.MODE_PRIVATE);
        if (sp.getInt("switch_tsdpopen", 0) == 0) {
            root.findViewById(R.id.tdc_open).setVisibility(View.GONE);
        }
        if (sp.getInt("switch_psd_code_open", 0) == 0) {
            root.findViewById(R.id.code_open).setVisibility(View.GONE);
        }
        if (sp.getInt("switch_psdpopen", 0) == 0) {
            root.findViewById(R.id.psd_open).setVisibility(View.GONE);
        }
        //初始化视图
        root.findViewById(R.id.psd_open).setOnClickListener(this);
        root.findViewById(R.id.tdc_open).setOnClickListener(this);
        root.findViewById(R.id.code_open).setOnClickListener(this);
        root.findViewById(R.id.cancel).setOnClickListener(this);
        mCameraDialog.setContentView(root);
        Window dialogWindow = mCameraDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
//        dialogWindow.setWindowAnimations(R.style.dialogstyle); // 添加动画
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = (int) getResources().getDisplayMetrics().widthPixels; // 宽度
        root.measure(0, 0);
        lp.height = root.getMeasuredHeight();

        lp.alpha = 9f; // 透明度
        dialogWindow.setAttributes(lp);
        mCameraDialog.show();
    }

    //弹出底部菜单
    private void setCallDialog() {
        mCameraDialog2 = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.call_bottom_dialog, null);
        //初始化视图
        root.findViewById(R.id.call_user).setOnClickListener(this);
        root.findViewById(R.id.call_Property).setOnClickListener(this);
        root.findViewById(R.id.cancel2).setOnClickListener(this);
        mCameraDialog2.setContentView(root);
        Window dialogWindow = mCameraDialog2.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
//        dialogWindow.setWindowAnimations(R.style.dialogstyle); // 添加动画
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = (int) getResources().getDisplayMetrics().widthPixels; // 宽度
        root.measure(0, 0);
        lp.height = root.getMeasuredHeight();

        lp.alpha = 9f; // 透明度
        dialogWindow.setAttributes(lp);
        mCameraDialog2.show();
    }

    //获得当前年月日
    public String getTimeYear() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String mYear = String.valueOf(c.get(Calendar.YEAR)); // 获取当前年份
        String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份
        String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码
        return mYear + "年" + mMonth + "月" + mDay + "日";
    }

    //获得当前时分
    public String getTimeHour() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));//获取当前周数
        String mHour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));//时
        String mMinute = String.valueOf(c.get(Calendar.MINUTE));//分
        if (mMinute.length() == 1) {
            mMinute = "0" + mMinute;
        }
        String mSecond = String.valueOf(c.get(Calendar.SECOND));//秒
        if (mSecond.length() == 1) {
            mSecond = "0" + mSecond;
        }
        return mHour + ":" + mMinute + ":" + mSecond;
    }

    //获取设备MAC地址
    public static String getLocalMac(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo winfo = wifi.getConnectionInfo();
        String mac = winfo.getMacAddress();
        return mac;
    }

    //获取当前时间
    public static Long getCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }

    //查看视频所需权限
    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i("checkSelfPermission>>>", "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

}
