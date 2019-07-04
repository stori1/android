package com.frd.hjc.videoplay1;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by HJC on 2019/7/3.
 */

public class MyRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener{

    private Context mContext;

    private int programHandle;
    private int vertexShader;
    private int fragmentShader;

    private int vPosition;
    private int afPosition;
//    private int mMVPMatrixHandle;
    private int samplerOES;
    private int textureId;

    private FloatBuffer vertexBuffer;
    private FloatBuffer fragmentBuffer;

    private SurfaceTexture surfaceTexture;
    private static Surface surface;
    private static boolean texPositionChanged;
    private OnRenderListener onRenderListener;

    //三角形的顶点着色器顶点
    private final float[] vertexPositionData = {
            -1f, -1f,//左下
            1f, -1f,//右下
            -1f, 1f,//左上
            1f, 1f//右上
    };

    //三角形的片段着色器顶点
    private float[] fragmentPositionData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };


    public MyRender(Context context){
        this.mContext =context;

        texPositionChanged = false;
        vertexBuffer = ByteBuffer.allocateDirect(vertexPositionData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexPositionData);
        vertexBuffer.position(0);

        fragmentBuffer = ByteBuffer.allocateDirect(MyApplication.fragmentData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(MyApplication.fragmentData);
        fragmentBuffer.position(0);

    }
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (onRenderListener != null){
            onRenderListener.onRender();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        initShader();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        renderMediacodec();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    private int initShader() {
        String fragmentShaderCode = "#version 100 \n";
        fragmentShaderCode += "#extension GL_OES_EGL_image_external:require \n";
        fragmentShaderCode += "precision mediump float; \n";
        fragmentShaderCode += "varying vec2 v_texPosition; \n";
        fragmentShaderCode += "uniform samplerExternalOES  sTexture; \n";
        fragmentShaderCode += "void main()\n";
        fragmentShaderCode += "{\n";
        fragmentShaderCode += " gl_FragColor=texture2D(sTexture, v_texPosition); \n";
        fragmentShaderCode += "}\n";

        String vertexShaderCode = "#version 100 \n";
        vertexShaderCode += "attribute vec4 vPosition;  \n";
        vertexShaderCode += "attribute vec2 af_Position;  \n";
        vertexShaderCode += "varying vec2 v_texPosition; \n";
//        vertexShaderCode += "uniform mat4 uMVPMatrix; \n";
        vertexShaderCode += "void main(){               \n";
        vertexShaderCode += "v_texPosition = af_Position; \n";
        vertexShaderCode += "gl_Position = vPosition; \n";
        vertexShaderCode += "}  \n";

        int[] arrayOfInt = new int[1];
        int i = compileShader(vertexShaderCode, GLES20.GL_VERTEX_SHADER);
        this.vertexShader = i;
        if(i == 0){
            Log.e("createShader Error: ", "failed when compileShader (vertexShader)");
        }
        int j = compileShader(fragmentShaderCode, GLES20.GL_FRAGMENT_SHADER);
        this.fragmentShader = j;
        if(j == 0){
            Log.e("createShader Error: ", "failed when compileShader (fragmentShader)");
        }
        this.programHandle = GLES20.glCreateProgram();
        GLES20.glAttachShader(this.programHandle, this.vertexShader);
        GLES20.glAttachShader(this.programHandle, this.fragmentShader);
        GLES20.glLinkProgram(this.programHandle);
        GLES20.glGetProgramiv(this.programHandle, GLES20.GL_LINK_STATUS, arrayOfInt, 0);
        if (arrayOfInt[0] == 0){
            Log.e("createShader Error:  ", "link program error: " + GLES20.glGetProgramInfoLog(this.programHandle));
            destroyShader();
        }
        vPosition = GLES20.glGetAttribLocation(this.programHandle, "vPosition");
        afPosition = GLES20.glGetAttribLocation(this.programHandle, "af_Position");
        samplerOES = GLES20.glGetUniformLocation(this.programHandle, "sTexture");
//        mMVPMatrixHandle = GLES20.glGetUniformLocation(this.programHandle, "uMVPMatrix");

        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        textureId = textureIds[0];

        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        surfaceTexture = new SurfaceTexture(textureId);
        surface = new Surface(surfaceTexture);
        surfaceTexture.setOnFrameAvailableListener(this);
        System.out.println("surface created success:  " + surface.hashCode());
        return 0;
    }

    private void renderMediacodec() {

//        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        surfaceTexture.updateTexImage();
        //修改纹理的坐标值，用于显示不同的区域
//        if (texPositionChanged){
////            float[] change = new float[8];
////            change[0] = 0.0f;
////            change[1] = 0.5f;
////            change[2] = 0.5f;
////            change[3] = 0.5f;
////            change[4] = 0f;
////            change[5] = 0f;
////            change[6] = 0.5f;
////            change[7] = 0f;
            this.fragmentBuffer.position(0);
            this.fragmentBuffer.put(MyApplication.fragmentData);
            this.fragmentBuffer.position(0);
//            System.out.println("修改了 fragmentBuffer 的值");
//        }else {
//            this.fragmentBuffer.position(0);
//            this.fragmentBuffer.put(this.fragmentPositionData);
//            this.fragmentBuffer.position(0);
//        }

        GLES20.glUseProgram(this.programHandle);
        GLES20.glEnableVertexAttribArray(this.vPosition);
        GLES20.glVertexAttribPointer(this.vPosition, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer);

        GLES20.glEnableVertexAttribArray(this.afPosition);
        GLES20.glVertexAttribPointer(this.afPosition, 2, GLES20.GL_FLOAT, false, 8, fragmentBuffer);
//        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, this.textureId);
        GLES20.glUniform1i(samplerOES, 0);
    }


    private int compileShader(String paramsString, int shaderType){
        int i = GLES20.glCreateShader(shaderType);

        if(i != 0){
            int[] arrayOfInt = new int[1];
            GLES20.glShaderSource(i, paramsString);
            GLES20.glCompileShader(i);
            GLES20.glGetShaderiv(i, GLES20.GL_COMPILE_STATUS, arrayOfInt, 0);
            if(arrayOfInt[0] == 0){
                Log.e("compileShader Error: ", " " + GLES20.glGetProgramInfoLog(i));
                GLES20.glDeleteShader(i);
                i = 0;
            }
        }
        return  i;
    }

    private void destroyShader() {
        if (this.programHandle != 0){
            GLES20.glDetachShader(this.programHandle, this.vertexShader);
            GLES20.glDetachShader(this.programHandle, this.fragmentShader);
            GLES20.glDeleteProgram(this.programHandle);
            this.programHandle = 0;
        }
        if (this.fragmentShader != 0){
            GLES20.glDeleteShader(this.fragmentShader);
            this.fragmentShader = 0;
        }
        if (this.vertexShader != 0){
            GLES20.glDeleteShader(this.vertexShader);
            this.vertexShader = 0;
        }
    }

    public void setOnRenderListener(OnRenderListener onRenderListener) {
        this.onRenderListener = onRenderListener;
    }


    public interface OnRenderListener {
        void onRender();
    }
    public Surface getSurface(){
        return surface;
    }

    public void setTexPositionChanged(boolean  changed){
        this.texPositionChanged =changed;
    }
}
