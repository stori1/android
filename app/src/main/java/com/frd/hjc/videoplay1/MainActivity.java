package com.frd.hjc.videoplay1;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private MyGLSurfaceView mSurfaceView;
    private MyCodec mDecoder;
    private ListenService.MyBinder mBinder;
    private Button SingleButton;
    private Button SpliceButton;
    private Button stopButton;

    private Button leftButton;
    private Button rightButton;
    private Button topButton;
    private Button downButton;


    private boolean isPlay;


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (ListenService.MyBinder) service;
            mBinder.listen();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = findViewById(R.id.glSurface);
        SingleButton = findViewById(R.id.singlePlay);
        SpliceButton = findViewById(R.id.SplicePlay);
        stopButton = findViewById(R.id.stop);

        leftButton = findViewById(R.id.moveLeft);
        rightButton = findViewById(R.id.moveRight);
        topButton = findViewById(R.id.moveTop);
        downButton = findViewById(R.id.moveDown);

        mSurfaceView = new MyGLSurfaceView(this);
        mDecoder = new MyCodec();
        MyApplication.needListen = true;
        MyApplication.deviceStatus = DeviceStatus.DEFAULT_MODE;
        requestPermission();
        Intent intent = new Intent(MainActivity.this, ListenService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
        SpliceButton.setOnClickListener(this);
        SingleButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
        topButton.setOnClickListener(this);
        downButton.setOnClickListener(this);
    }

    private void startWork() {
        isPlay = false;
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                while (MyApplication.needListen) {
                    switch (MyApplication.deviceStatus) {
                        case SINGLE_MODE:
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
//                            mSurfaceView.getRender().setTexPositionChanged(false);
                            break;
                        case SPLICE_MODE:

//                            mSurfaceView.getRender().setTexPositionChanged(true);

                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
//                            MyApplication.needListen = false;
                            break;
                        default:
                            mDecoder.close();
                            break;
                    }
                    if ((!isPlay && (MyApplication.deviceStatus == DeviceStatus.SINGLE_MODE))
                            || (!isPlay && (MyApplication.deviceStatus == DeviceStatus.SPLICE_MODE))){
                        mDecoder.readVideoFile(path + MyApplication.fileName , mSurfaceView.getRender().getSurface());
                        mDecoder.playVideo();
                        isPlay = true;
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
        unbindService(connection);
        MyApplication.deviceStatus = DeviceStatus.DEFAULT_MODE;
        MyApplication.needListen = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startWork();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    System.out.println("申请读写权限成功");
                }else {
                    System.out.println("申请读写权限失败");
                }
                break;
            default:
                break;
        }
    }

    private void requestPermission(){
        int writePermissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        String[] permissions = new String[2];
        permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        permissions[1] = Manifest.permission.READ_EXTERNAL_STORAGE;
        //两者都没有权限。
        if ((writePermissionCheck != 0) || (readPermissionCheck != 0)){
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.singlePlay:
                MyApplication.fragmentData[0] = 0f;
                MyApplication.fragmentData[1] = 1f;
                MyApplication.fragmentData[2] = 1f;
                MyApplication.fragmentData[3] = 1f;
                MyApplication.fragmentData[4] = 0f;
                MyApplication.fragmentData[5] = 0f;
                MyApplication.fragmentData[6] = 1f;
                MyApplication.fragmentData[7] = 0f;
                MyApplication.deviceStatus = DeviceStatus.SINGLE_MODE;
                break;
            case R.id.SplicePlay:
                MyApplication.fragmentData[0] = 0f;
                MyApplication.fragmentData[1] = 1f;
                MyApplication.fragmentData[2] = 0.5f;
                MyApplication.fragmentData[3] = 1f;
                MyApplication.fragmentData[4] = 0f;
                MyApplication.fragmentData[5] = 0.5f;
                MyApplication.fragmentData[6] = 0.5f;
                MyApplication.fragmentData[7] = 0.5f;
                MyApplication.deviceStatus = DeviceStatus.SPLICE_MODE;
                break;
            case R.id.stop:
                MyApplication.deviceStatus = DeviceStatus.DEFAULT_MODE;
                mDecoder.close();
                break;
            case R.id.moveLeft:
                MyApplication.fragmentData[0] += 0.01f;
                MyApplication.fragmentData[2] += 0.01f;
                MyApplication.fragmentData[4] += 0.01f;
                MyApplication.fragmentData[6] += 0.01f;
                break;
            case R.id.moveRight:
                MyApplication.fragmentData[0] -= 0.01f;
                MyApplication.fragmentData[2] -= 0.01f;
                MyApplication.fragmentData[4] -= 0.01f;
                MyApplication.fragmentData[6] -= 0.01f;
                break;
            case R.id.moveTop:
                MyApplication.fragmentData[1] += 0.01f;
                MyApplication.fragmentData[3] += 0.01f;
                MyApplication.fragmentData[5] += 0.01f;
                MyApplication.fragmentData[7] += 0.01f;
                break;
            case R.id.moveDown:
                MyApplication.fragmentData[1] -= 0.01f;
                MyApplication.fragmentData[3] -= 0.01f;
                MyApplication.fragmentData[5] -= 0.01f;
                MyApplication.fragmentData[7] -= 0.01f;
                break;
            default:
                break;
        }
    }

}
