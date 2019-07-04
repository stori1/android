package com.frd.hjc.videoplay1;

import android.app.Application;

/**
 * Created by HJC on 2019/7/1.
 */

public class MyApplication extends Application{

    /**
     * 0  default mode  1  single mode  2  splice mode  3 remote mode
     * */
    public static DeviceStatus deviceStatus;
    public static String fileName = "/Download/2c91f5062c63d055c6f50badab5e921c.mp4";
    public static boolean needListen;

    public static float[] fragmentData = {
            0f, 0.5f,
            0.5f, 0.5f,
            0f, 0f,
            0.5f, 0f
    };

    @Override
    public void onCreate() {
        super.onCreate();
        deviceStatus = DeviceStatus.DEFAULT_MODE;
    }
}
