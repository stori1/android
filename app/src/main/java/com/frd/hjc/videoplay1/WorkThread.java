package com.frd.hjc.videoplay1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by HJC on 2019/7/1.
 */

class WorkThread extends Thread{

    private Socket socket;

    public WorkThread(Socket socket){
        this.socket = socket;
    }

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
            String response = task(sb.toString(), socket);
            OutputStream ops = socket.getOutputStream();
            ops.write(response != null ? response.getBytes() : new byte[0]);
            ops.flush();
            ops.close();
            socket.shutdownOutput();
            socket.shutdownInput();
            bufReader.close();
            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String task(String s, Socket socket){
        if(s.startsWith("SINGLE")){
            MyApplication.deviceStatus = DeviceStatus.SINGLE_MODE;
            MyApplication.fileName = "aaa";
        }else if (s.startsWith("SPLICE")){
            MyApplication.deviceStatus = DeviceStatus.SPLICE_MODE;
            MyApplication.fileName = "aaa";
        }
        return null;
    }
}
