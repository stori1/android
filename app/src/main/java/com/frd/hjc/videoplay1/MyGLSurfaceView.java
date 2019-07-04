package com.frd.hjc.videoplay1;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by HJC on 2019/7/3.
 */

public class MyGLSurfaceView extends GLSurfaceView{

    private MyRender mRender;

    public MyGLSurfaceView(Context context) {
        this(context, null);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        mRender = new MyRender(context);
        setRenderer(mRender);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        mRender.setOnRenderListener(new MyRender.OnRenderListener() {
            @Override
            public void onRender() {
                requestRender();
            }
        });
    }

    public MyRender getRender(){
        return mRender;
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
