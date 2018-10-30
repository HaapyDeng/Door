package com.max_plus.homedooropenplate.activity;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.max_plus.homedooropenplate.R;
import com.max_plus.homedooropenplate.RegisterActivity;

public class SettingActivity extends Activity implements View.OnClickListener {
    private ImageView iv_setback;
    private TextView tv_set_time, tv_setloginpsd, tv_setopenpsd, tv_setopenchoose, bt_facer, bt_openblue;

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

        bt_facer = findViewById(R.id.bt_facer);
        bt_facer.setOnClickListener(this);

        bt_openblue = findViewById(R.id.bt_openblue);
        bt_openblue.setOnClickListener(this);
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
            case R.id.bt_facer:
                Intent fintent = new Intent("android.media.action.IMAGE_CAPTURE");
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                ((com.max_plus.homedooropenplate.Application) (SettingActivity.this.getApplicationContext())).setCaptureImage(uri);
                Log.d("url==>>>", "" + uri);
                fintent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(fintent, 1);
                break;
            case R.id.bt_openblue:
//                Intent intent3 = new Intent(this, BluetoothActivity.class);
//                startActivity(intent3);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri mPath = ((com.max_plus.homedooropenplate.Application) (SettingActivity.this.getApplicationContext())).getCaptureImage();
            String file = getPath(mPath);
            Bitmap bmp = com.max_plus.homedooropenplate.Application.decodeImage(file);
            startRegister(bmp, file);
        } else if (requestCode == 4) {
            if (data == null) {
                Toast.makeText(this, "未识别，人脸注册失败", Toast.LENGTH_LONG).show();
                return;
            }
            int faceCode = data.getIntExtra("rface", 2);
            Log.d("faceCode==>>>", "" + faceCode);
            if (faceCode == 1) {
                Toast.makeText(this, "人脸注册成功", Toast.LENGTH_LONG).show();
                return;
            } else {
                Toast.makeText(this, "人脸注册失败", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    /**
     * @param mBitmap
     */
    private void startRegister(Bitmap mBitmap, String file) {
        Intent it = new Intent(SettingActivity.this, RegisterActivity.class);
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

}
