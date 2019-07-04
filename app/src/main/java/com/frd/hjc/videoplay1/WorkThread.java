package com.frd.hjc.videoplay1;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;

/**
 * Created by HJC on 2019/7/1.
 */

class WorkThread extends Thread{
    private static final String LAG = "WorkThread";

    private Socket socket;

    public WorkThread(Socket socket){
        this.socket = socket;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void run() {
        super.run();
        try {
            InputStream is = socket.getInputStream();
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader bufReader = new BufferedReader(reader);
            String s = null;
            StringBuffer sb = new StringBuffer();
            while ((s = bufReader.readLine()) != null){
                sb.append(s);
            }
            Log.e(LAG, sb.toString());
            String response = task(sb.toString(), socket);
            OutputStream ops = socket.getOutputStream();
            ops.write(!Objects.equals(response, "") ? response.getBytes() : new byte[0]);
            ops.flush();
            socket.shutdownOutput();
            socket.shutdownInput();
            ops.close();
            bufReader.close();
            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String task(String s, Socket socket){
        if(s.contains("SINGLE")){
            MyApplication.fragmentData[0] = 0f;
            MyApplication.fragmentData[1] = 1f;
            MyApplication.fragmentData[2] = 1f;
            MyApplication.fragmentData[3] = 1f;
            MyApplication.fragmentData[4] = 0f;
            MyApplication.fragmentData[5] = 0f;
            MyApplication.fragmentData[6] = 1f;
            MyApplication.fragmentData[7] = 0f;
            MyApplication.deviceStatus = DeviceStatus.SINGLE_MODE;
//            MyApplication.fileName = "aaa";
            return "SINGLE";
        }else if (s.contains("SPLICE")){
            MyApplication.fragmentData[0] = 0f;
            MyApplication.fragmentData[1] = 1f;
            MyApplication.fragmentData[2] = 0.5f;
            MyApplication.fragmentData[3] = 1f;
            MyApplication.fragmentData[4] = 0f;
            MyApplication.fragmentData[5] = 0.5f;
            MyApplication.fragmentData[6] = 0.5f;
            MyApplication.fragmentData[7] = 0.5f;
            MyApplication.deviceStatus = DeviceStatus.SPLICE_MODE;
//            MyApplication.fileName = "aaa";
            return "SPLICE";
        }else if (s.contains("left")){
            MyApplication.fragmentData[0] += 0.01f;
            MyApplication.fragmentData[2] += 0.01f;
            MyApplication.fragmentData[4] += 0.01f;
            MyApplication.fragmentData[6] += 0.01f;
            return "LEFT";
        }else if (s.contains("right")){
            MyApplication.fragmentData[0] -= 0.01f;
            MyApplication.fragmentData[2] -= 0.01f;
            MyApplication.fragmentData[4] -= 0.01f;
            MyApplication.fragmentData[6] -= 0.01f;
            return "RIGHT";
        }
        else if (s.contains("top")){
            MyApplication.fragmentData[1] += 0.01f;
            MyApplication.fragmentData[3] += 0.01f;
            MyApplication.fragmentData[5] += 0.01f;
            MyApplication.fragmentData[7] += 0.01f;
            return "TOP";
        }
        else if (s.contains("down")){
            MyApplication.fragmentData[1] -= 0.01f;
            MyApplication.fragmentData[3] -= 0.01f;
            MyApplication.fragmentData[5] -= 0.01f;
            MyApplication.fragmentData[7] -= 0.01f;
            return "DOWN";
        }
        return "AAA";
    }
}
