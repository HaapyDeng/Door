package com.max_plus.homedooropenplate.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.client.android.AutoScannerView;
import com.max_plus.homedooropenplate.R;
import com.google.zxing.client.android.BaseCaptureActivity;

public class Capture2Activity extends BaseCaptureActivity {
    private static final String TAG = Capture2Activity.class.getSimpleName();

    private SurfaceView surfaceView;
    private AutoScannerView autoScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture2);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        autoScannerView = (AutoScannerView) findViewById(R.id.autoscanner_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        autoScannerView.setCameraManager(cameraManager);
    }


    @Override
    public SurfaceView getSurfaceView() {
        return (surfaceView == null) ? (SurfaceView) findViewById(R.id.preview_view) : surfaceView;
    }

    @Override
    public void dealDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        Log.i(TAG, "dealDecode ~~~~~ " + rawResult.getText() + " " + barcode + " " + scaleFactor);
        playBeepSoundAndVibrate(true, false);
        Toast.makeText(this, rawResult.getText(), Toast.LENGTH_LONG).show();
        Bundle bundle = new Bundle();
        bundle.putString("result", rawResult.getText());
        Intent intent = new Intent();
        if (rawResult.getText() == null) {
            intent.putExtra("result", "0");
        } else {
            intent.putExtra("result", rawResult.getText());
        }
        setResult(0, intent);
        finish();
//        对此次扫描结果不满意可以调用
//        reScan();
    }
}
