package com.example.kingwen.dobot130.Application;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import com.iflytek.cloud.SpeechUtility;



/**
 * 全局变量，用于程序的参数初始化
 */
public class MyApplication extends Application {


    /**
     * 用于更新UI和传递参数的全局handler
     */
    private Handler mHandler;


    @Override
    public void onCreate() {

        //讯飞语音识别模块
        SpeechUtility.createUtility(MyApplication.this, "appid=" + "56f7edc7");
        Log.e("creat", "hello");

        super.onCreate();
    }

    public Handler getmHandler() {
        return mHandler;
    }

    public void setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }


}
