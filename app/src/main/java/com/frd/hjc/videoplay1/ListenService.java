package com.frd.hjc.videoplay1;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by HJC on 2019/7/1.
 */

public class ListenService extends Service{

    private static final String LAG = "ListenService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        listen();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void listen(){
            Log.e(LAG, "start listening*****");
            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    try {
                        ServerSocket serverSocket = new ServerSocket(9999);
                        Log.e(LAG, "start listening: " + serverSocket.getInetAddress());
                        while (MyApplication.needListen){
                            Socket socket = serverSocket.accept();
                            WorkThread thread = new WorkThread(socket);
                            thread.run();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
    }
}
