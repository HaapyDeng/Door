package com.max_plus.homedooropenplate;

import android.util.Log;

import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.guo.android_extend.java.ExtInputStream;
import com.guo.android_extend.java.ExtOutputStream;
import com.max_plus.homedooropenplate.activity.LoginActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by djz on 2018/5/17.
 */

public class FaceDB {
    private final String TAG = this.getClass().toString();

    public static String age_key = "ECSGoH9wTBVcmuoDDFqEuaCfFxx6HZ3ZSqwvmZoDoYwT";
    public static String gender_key = "ECSGoH9wTBVcmuoDDFqEuaCnRNDFTfxvsywyMfKqveSj";
    public static String appid = "7Q9NG1WNBwTa6f7J5eUoaUHoYj7Euoc3JW5vH17gQSgD";
    public static String ft_key = "ECSGoH9wTBVcmuoDDFqEuaBo8A7sEizmUKKqzKFr9Uwh";
    public static String fd_key = "ECSGoH9wTBVcmuoDDFqEuaBvHZP7ScXEAdDJ1i2RGUXA";
    public static String fr_key = "ECSGoH9wTBVcmuoDDFqEuaCQwARkCNt9RAmu5eNwieSb";

    String mDBPath;
    public List<FaceRegist> mRegister;
    AFR_FSDKEngine mFREngine;
    AFR_FSDKVersion mFRVersion;
    boolean mUpgrade;

    class FaceRegist {
        String mName;
        List<AFR_FSDKFace> mFaceList;

        public FaceRegist(String name) {
            mName = name;
            mFaceList = new ArrayList<>();
        }
    }

    public FaceDB(String path) {
        mDBPath = path;
        mRegister = new ArrayList<>();
        mFRVersion = new AFR_FSDKVersion();
        mUpgrade = false;
        mFREngine = new AFR_FSDKEngine();
        AFR_FSDKError error = mFREngine.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
        if (error.getCode() != AFR_FSDKError.MOK) {
            Log.e(TAG, "AFR_FSDK_InitialEngine fail! error code :" + error.getCode());
        } else {
            mFREngine.AFR_FSDK_GetVersion(mFRVersion);
            Log.d(TAG, "AFR_FSDK_GetVersion=" + mFRVersion.toString());
        }
    }

    public void destroy() {
        if (mFREngine != null) {
            mFREngine.AFR_FSDK_UninitialEngine();
        }
    }

    private boolean saveInfo() {
        try {
            FileOutputStream fs = new FileOutputStream(mDBPath + "/face.txt");
            ExtOutputStream bos = new ExtOutputStream(fs);
            bos.writeString(mFRVersion.toString() + "," + mFRVersion.getFeatureLevel());
            bos.close();
            fs.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean loadInfo() {
        if (!mRegister.isEmpty()) {
            return false;
        }
        try {
            FileInputStream fs = new FileInputStream(mDBPath + "/face.txt");
            ExtInputStream bos = new ExtInputStream(fs);
            //load version
            String version_saved = bos.readString();
            if (version_saved.equals(mFRVersion.toString() + "," + mFRVersion.getFeatureLevel())) {
                mUpgrade = true;
            }
            //load all regist name.
            FileInputStream fsf = new FileInputStream(mDBPath + "/facef.txt");
            ExtInputStream bosf = new ExtInputStream(fsf);
            for (String name = bosf.readString(); name != null; name = bosf.readString()) {
                if (new File(mDBPath + "/" + name + ".dataf").exists()) {
                    mRegister.add(new FaceRegist(new String(name)));
                }
            }
//            if (version_saved != null) {
//                for (String name = bos.readString(); name != null; name = bos.readString()) {
//                    if (new File(mDBPath + "/" + name + ".data").exists()) {
//                        mRegister.add(new FaceRegist(new String(name)));
//                    }
//                }
//            }
            bos.close();
            fs.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean loadFaces() {
        Log.d("mDBPath,>>>", mDBPath);
        loadInfo();
//        if (loadInfo()) {
        try {
            for (FaceRegist face : mRegister) {
                Log.d(TAG, "load name:" + face.mName + "'s face feature data.");
                FileInputStream fs = new FileInputStream(mDBPath + "/" + face.mName + ".dataf");
                ExtInputStream bos = new ExtInputStream(fs);
                AFR_FSDKFace afr = null;
                do {
                    if (afr != null) {
                        if (mUpgrade) {
                            //upgrade data.
                        }
                        face.mFaceList.add(afr);
                    }
                    afr = new AFR_FSDKFace();
                } while (bos.readBytes(afr.getFeatureData()));
                bos.close();
                fs.close();
                Log.d(TAG, "load name: size = " + face.mFaceList.size());
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        }
        return false;
    }

    public void addFace(String name, AFR_FSDKFace face) {
        try {
            //check if already registered.
            boolean add = true;
            for (FaceRegist frface : mRegister) {
                if (frface.mName.equals(name)) {
                    frface.mFaceList.add(face);
                    add = false;
                    break;
                }
            }
            if (add) { // not registered.
                FaceRegist frface = new FaceRegist(name);
                frface.mFaceList.add(face);
                mRegister.add(frface);
            }

            if (saveInfo()) {
                Log.d("DBPATH==", mDBPath);
                //update all names
                FileOutputStream fs = new FileOutputStream(mDBPath + "/facef.txt", true);
                ExtOutputStream bos = new ExtOutputStream(fs);
                for (FaceRegist frface : mRegister) {
                    bos.writeString(frface.mName);
                }
                bos.close();
                fs.close();

                //save new feature
                fs = new FileOutputStream(mDBPath + "/" + name + ".dataf", true);
                bos = new ExtOutputStream(fs);
                bos.writeBytes(face.getFeatureData());
                bos.close();
                fs.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean delete(String name) {
        try {
            //check if already registered.
            boolean find = false;
            for (FaceRegist frface : mRegister) {
                if (frface.mName.equals(name)) {
                    File delfile = new File(mDBPath + "/" + name + ".data");
                    if (delfile.exists()) {
                        delfile.delete();
                    }
                    mRegister.remove(frface);
                    find = true;
                    break;
                }
            }

            if (find) {
                if (saveInfo()) {
                    //update all names
                    FileOutputStream fs = new FileOutputStream(mDBPath + "/face.txt", true);
                    ExtOutputStream bos = new ExtOutputStream(fs);
                    for (FaceRegist frface : mRegister) {
                        bos.writeString(frface.mName);
                    }
                    bos.close();
                    fs.close();
                }
            }
            return find;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean upgrade() {
        return false;
    }
}
