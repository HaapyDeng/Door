package com.max_plus.homedooropenplate;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.max_plus.homedooropenplate.activity.killSelfService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Tools {
    // 项目文件根目录

    public static final String FILEDIR = "/back";

    // 照相机照片目录

    public static final String FILEPHOTO = "/photos";

    // 应用程序图片存放

    public static final String FILEIMAGE = "/images";

    // 应用程序缓存

    public static final String FILECACHE = "/cache";

    // 用户信息目录

    public static final String FILEUSER = "user";

    //注册face目录

    public static final String FACE = "/face";

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
    /*

     * 检查sd卡是否可用

     * getExternalStorageState 获取状态

     * Environment.MEDIA_MOUNTED 直译  环境媒体登上  表示，当前sd可用

     */

    public static boolean checkSdCard() {

        if (Environment.getExternalStorageState().equals(

                Environment.MEDIA_MOUNTED))

            //sd卡可用

            return true;

        else

            //当前sd卡不可用

            return false;

    }

    /*

     * 获取sd卡的文件路径

     * getExternalStorageDirectory 获取路径

     */

    public static String getSdPath() {

        return Environment.getExternalStorageDirectory() + "/";

    }

    /*

     * 创建一个文件夹

     */

    public static void createFileDir(String fileDir) {

        String path = getSdPath() + fileDir;

        File path1 = new File(path);

        if (!path1.exists())

        {

            path1.mkdirs();

            Log.d("path", "创建");

        }

    }

    //判断文件是否存在
    public static boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }

        return true;

    }

    /**
     * 根据行读取内容
     *
     * @return
     */
    public List<String> getNameList() {
        //将读出来的一行行数据使用List存储
        String filePath = Environment.getExternalStorageDirectory() + "/face/facef.txt";

        List newList = new ArrayList<String>();
        try {
            File file = new File(filePath);
            int count = 0;//初始化 key值
            if (file.isFile() && file.exists()) {//文件存在
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
                BufferedReader br = new BufferedReader(isr);
                String lineTxt = null;
                while ((lineTxt = br.readLine()) != null) {
                    if (!"".equals(lineTxt)) {
                        String reds = lineTxt.split("\\+")[0];  //java 正则表达式
                        newList.add(count, reds);
                        count++;
                    }
                }
                isr.close();
                br.close();
            } else {
                Log.d("can not find file", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newList;
    }
    /**
     * 此工具类用来重启APP，只是单纯的重启，不做任何处理。
     * Created by 13itch on 2016/8/5.
     */

        /**
         * 重启整个APP
         * @param context
         * @param Delayed 延迟多少毫秒
         */
        public static void restartAPP(Context context,long Delayed){

            /**开启一个新的服务，用来重启本APP*/
            Intent intent1=new Intent(context,killSelfService.class);
            intent1.putExtra("com.max_plus.homedooropenplate",context.getPackageName());
            intent1.putExtra("Delayed",Delayed);
            context.startService(intent1);

            /**杀死整个进程**/
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        /***重启整个APP*/
        public static void restartAPP(Context context){
            restartAPP(context,2000);
        }


}
